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
        change_to_metrics = {}
        for (full_metrics_name, metrics_obj) in bench_obj['secondaryMetrics'].items():
            full_metrics_name: str
            metric_name_colon_index: int = full_metrics_name.find(':')
            if metric_name_colon_index == -1: continue
            change_name: str = full_metrics_name[:metric_name_colon_index]
            metric_name: str = full_metrics_name[metric_name_colon_index + 1:]
            metric_score: float = metrics_obj['score']
            change_to_metrics.setdefault(change_name, {})[metric_name] = metric_score
        for (change_name, metrics_series_dict) in change_to_metrics.items():
            final_series_dict = series_dict.copy()
            final_series_dict.update(metrics_series_dict)
            series.append(pd.Series(final_series_dict, name=change_name))
    return pd.DataFrame(series)


data = create_long_form_dataframe_from_json('../../../build/result.json')


# Plot

def create_bar_figure(data, **kwargs):
    fig = px.bar(data, **kwargs)
    fig.update_layout(
        barmode='group',
        xaxis_title='Change',
        legend=dict(title_text=None, orientation='h', yanchor='bottom', y=1.00, xanchor='right', x=1.00),
    )
    return fig


def create_simple_bar_figure(data, y, title, yaxis_title, texttemplate='%{value}', color='benchmark_name'):
    fig = create_bar_figure(data, y=y, color=color)
    fig.update_traces(texttemplate=texttemplate)
    fig.update_layout(title=title, yaxis_title=yaxis_title)
    return fig


time_per_change = create_simple_bar_figure(
    data, y='systemNanoTime', texttemplate='%{value:.2f}s', title='Time per change', yaxis_title='Time (seconds)'
)
executed_tasks_per_change = create_simple_bar_figure(
    data, y='executedTasks', title='Executed tasks per change', yaxis_title='Executed tasks'
)
required_tasks_per_change = create_simple_bar_figure(
    data, y='requiredTasks', title='Required tasks per change', yaxis_title='Required tasks'
)
provided_resource_dependencies_per_change = create_simple_bar_figure(
    data, y='providedResourceDependencies', title='Provided resource dependencies per change', yaxis_title='Provided resource dependencies'
)



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
    single_row_col_graph(figure=time_per_change),
    single_row_col_graph(figure=executed_tasks_per_change),
    single_row_col_graph(figure=required_tasks_per_change),
    single_row_col_graph(figure=provided_resource_dependencies_per_change),
    html.H2("Raw data"),
    html.Hr(),
    single_row_col(dbc.Table.from_dataframe(data, striped=True, bordered=True, hover=True, size='sm')),
], fluid=True)
app.run_server(debug=True)
