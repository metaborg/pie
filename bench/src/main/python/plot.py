import argparse
import json
import os

import dash
import dash_bootstrap_components as dbc  # https://dash-bootstrap-components.opensource.faculty.ai/
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd
import plotly.express as px
import plotly.graph_objects as go


def main():
    script_dir = os.path.dirname(os.path.realpath(__file__))
    parser = argparse.ArgumentParser(description='PIE benchmark plotter')
    parser.add_argument(
        '--input-file',
        dest='input_file',
        default='{}/../../../build/reports/jmh/result.json'.format(script_dir),
        type=str,
        help='Input JMH result file'
    )
    subparsers = parser.add_subparsers(title='subcommands', dest='subcommand')
    subparsers.add_parser('dash', help='Starts a live Dash application with benchmark results')
    export_html_parser = subparsers.add_parser('export-html', help='Exports benchmark results as a static HTML page')
    export_html_parser.add_argument(
        '--output-file',
        dest='output_file',
        default='{}/../../../build/reports/jmh/result.html'.format(script_dir),
        type=str,
        help='Output HTML file'
    )
    args = parser.parse_args()

    data = create_long_form_dataframe_from_json(args.input_file)
    figs = [
        create_incrementality_figure(data, 'calc', 'validation'),
        create_incrementality_figure(data, 'chars', 'validation'),
        create_layer_figure(data, 'calc'),
        create_layer_figure(data, 'chars'),
        create_raw_data_figure(data),
    ]

    if args.subcommand == 'dash':
        start_dash_app(figs)
    elif args.subcommand == 'export-html':
        export_html(args.output_file, figs)


def create_long_form_dataframe_from_json(path: str) -> pd.DataFrame:
    with open(path) as json_file:
        json_data = json.load(json_file)
    series = []
    for bench_obj in json_data:
        series_dict = {}
        benchmark: str = bench_obj['benchmark']
        series_dict['benchmark'] = benchmark.replace("mb.pie.bench.spoofax3.Spoofax3Bench.", "")
        params_obj = bench_obj['params']
        series_dict['language'] = params_obj['language']
        series_dict['layer'] = params_obj['layer']
        for (full_metrics_name, metrics_obj) in bench_obj['secondaryMetrics'].items():
            full_metrics_name: str
            metric_name_colon_index: int = full_metrics_name.find(':')
            if metric_name_colon_index == -1: continue
            change_name: str = full_metrics_name[:metric_name_colon_index]
            metric_name: str = full_metrics_name[metric_name_colon_index + 1:]
            final_series_dict = series_dict.copy()
            final_series_dict['variable'] = metric_name
            final_series_dict['value'] = metrics_obj['score']
            final_series_dict['error'] = metrics_obj['scoreError']
            final_series_dict['unit'] = metrics_obj['scoreUnit']
            series.append(pd.Series(final_series_dict, name=change_name))
    data = pd.DataFrame(series)
    data.index.name = 'change'
    return data


def create_incrementality_figure(data: pd.DataFrame, language: str, layer: str):
    variables = {
        'systemNanoTime': 'Time',
        'requiredTasks': 'Required tasks',
        'executedTasks': 'Executed tasks',
        'requiredResourceDependencies': 'Required resource dependencies',
        'providedResourceDependencies': 'Provided resource dependencies'
    }
    variables_keys = list(variables.keys())
    fig = px.bar(
        data.query('variable in @variables and language == @language and layer == @layer'),
        y='value',
        error_y='error',
        color='benchmark',
        facet_row='variable',
        height=1500,
        category_orders={'variable': variables_keys}
    )
    fig.update_traces(texttemplate='%{value}')
    fig.update_traces(texttemplate='%{value:.2f}s', row=0)
    fig.update_layout(
        barmode='group',
        title='Incrementality comparison (language={}, layer={})'.format(language, layer),
        legend=dict(title_text=None, orientation='h', yanchor='bottom', y=1.00, xanchor='right', x=1.00),
    )

    def update_annotation(a):
        name = a.text.split("=")[-1]
        text: str
        if name in variables:
            text = variables[name]
        else:
            text = name
        a.update(text=text)

    fig.for_each_annotation(update_annotation)
    fig.update_yaxes(matches=None, title=None)
    return fig


def create_layer_figure(data: pd.DataFrame, language: str):
    variables = {'systemNanoTime': 'Time'}
    variables_keys = list(variables.keys())
    layers = {'validation': 'Validation', 'noop': 'None'}
    layers_keys = list(layers.keys())
    go.Bar
    fig = px.bar(
        data.query('variable in @variables and language == @language'),
        y='value',
        error_y='error',
        color='benchmark',
        facet_row='layer',
        height=500,
        category_orders={'layer': layers_keys}
    )
    fig.update_traces(texttemplate='%{value:.2f}s')
    fig.update_layout(
        barmode='group',
        title='Layer comparison (language={})'.format(language),
        legend=dict(title_text=None, orientation='h', yanchor='bottom', y=1.00, xanchor='right', x=1.00),
    )

    def update_annotation(a):
        name = a.text.split("=")[-1]
        text: str
        if name in variables:
            text = variables[name]
        elif name in layers:
            text = layers[name]
        else:
            text = name
        a.update(text=text)

    fig.for_each_annotation(update_annotation)
    fig.update_yaxes(matches=None, title=None)
    return fig


def create_raw_data_figure(data: pd.DataFrame):
    data = data.reset_index()
    fig = go.Figure(data=[go.Table(
        header=dict(
            values=list(data.columns),
            align='left'
        ),
        cells=dict(
            values=data.transpose().values.tolist(),
            align='left'
        )
    )])
    fig.update_layout(title='Raw data', height=1000)
    return fig


def start_dash_app(figs: [go.Figure]):
    app = dash.Dash(external_stylesheets=[dbc.themes.BOOTSTRAP])
    elements = [html.H1("PIE benchmarks"), html.Hr()]
    for fig in figs:
        elements.append(single_row_col_graph(figure=fig))
    app.layout = dbc.Container(elements, fluid=True)
    app.run_server(debug=True)


def single_row_col(children):
    return dbc.Row(dbc.Col(html.Div(children)))


def single_row_col_graph(figure: go.Figure):
    return single_row_col(dcc.Graph(figure=figure))


def export_html(output_file: str, figs: [go.Figure]):
    with open(output_file, 'w') as f:
        f.write("<html><head></head><body>" + "\n")
        add_js = True
        for fig in figs:
            fig: go.Figure
            f.write(fig.to_html(full_html=False, include_plotlyjs=add_js))
            add_js = False
        f.write("</body></html>" + "\n")


if __name__ == "__main__":
    main()
