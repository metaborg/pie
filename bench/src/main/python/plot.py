import pandas as pd
import plotly.express as px
import json

# Read JMH JSON result
with open('../../../build/result.json') as json_file:
    json_data = json.load(json_file)

# Dictionaries with data to collect
time_per_change_per_category = {}

# Collect data
for bench in json_data:
    benchmark_name: str = bench['benchmark']
    full_metrics_name: str
    for (full_metrics_name, metrics_object) in bench['secondaryMetrics'].items():
        metric_name_colon_index: int = full_metrics_name.find(':')
        if metric_name_colon_index > 0:
            change_name: str = full_metrics_name[:metric_name_colon_index]
            metric_name: str = full_metrics_name[metric_name_colon_index + 1:]
            metric_score: float = metrics_object['score']
            if metric_name == 'systemNanoTime':
                time_per_change_per_category.setdefault(benchmark_name, {})[change_name] = metric_score

# Plot figures
time_per_change_per_category_df = pd.DataFrame(time_per_change_per_category)
time_per_change_per_category_fig = px.bar(time_per_change_per_category_df)
time_per_change_per_category_fig.update_traces(
    texttemplate='%{value:.2f}s',
)
time_per_change_per_category_fig.update_layout(
    title='Time per change per benchmark',
    xaxis=dict(
        title='Change name',
    ),
    yaxis=dict(
        title='Time (seconds)',
    ),
    legend=dict(
        x=1.0, y=1.0,
    ),
    barmode='group',
)
time_per_change_per_category_fig.show()
