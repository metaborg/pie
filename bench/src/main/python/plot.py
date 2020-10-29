import json

import dash
import dash_bootstrap_components as dbc  # https://dash-bootstrap-components.opensource.faculty.ai/
import dash_core_components as dcc
import dash_html_components as html
import pandas as pd
import plotly.express as px


# Gather data

def create_long_form_dataframe_from_json(path: str):
    with open(path) as json_file:
        json_data = json.load(json_file)
    series = []
    for bench_obj in json_data:
        series_dict = {}
        benchmark_name: str = bench_obj['benchmark']
        series_dict['benchmark_name'] = benchmark_name.replace("mb.pie.bench.spoofax3.Spoofax3Bench.", "")
        params_obj = bench_obj['params']
        series_dict['layer_name'] = params_obj['layerKind']
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


data = create_long_form_dataframe_from_json('../../../build/result.json')

# Plot

incrementality_facets = {
    'systemNanoTime': 'Time',
    'requiredTasks': 'Required tasks',
    'executedTasks': 'Executed tasks',
    'requiredResourceDependencies': 'Required resource dependencies',
    'providedResourceDependencies': 'Provided resource dependencies'
}
incrementality_facets_keys = list(incrementality_facets.keys())
incrementality_data = data[data.variable.isin(incrementality_facets_keys)]
incrementality = px.bar(
    incrementality_data, y='value', error_y='error', color='benchmark_name', facet_row='variable', facet_col_wrap=1,
    height=1500,
    category_orders={'variable': incrementality_facets_keys}
)
incrementality.update_traces(texttemplate='%{value}')
incrementality.update_traces(texttemplate='%{value:.2f}s', row=0)
incrementality.update_layout(
    barmode='group',
    legend=dict(title_text=None, orientation='h', yanchor='bottom', y=1.00, xanchor='right', x=1.00),
)
incrementality.for_each_annotation(lambda a: a.update(text=incrementality_facets[a.text.split("=")[-1]]))
incrementality.update_yaxes(matches=None, title=None)


# Render

def single_row_col(children):
    return dbc.Row(dbc.Col(html.Div(children)))


def single_row_col_graph(figure):
    return single_row_col(dcc.Graph(figure=figure))


app = dash.Dash(external_stylesheets=[dbc.themes.BOOTSTRAP])
app.layout = dbc.Container([
    html.H1("PIE benchmarks"),
    html.Hr(),
    html.H2("Incrementality"),
    html.Hr(),
    single_row_col_graph(figure=incrementality),
    html.H2("Raw data"),
    html.Hr(),
    single_row_col(dbc.Table.from_dataframe(data, striped=True, bordered=True, hover=True, size='sm')),
], fluid=True)
app.run_server(debug=True)
