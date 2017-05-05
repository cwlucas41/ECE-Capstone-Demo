import pandas as pd
import numpy as np
import plotly as plotly
import plotly.plotly as py
import plotly.graph_objs as go
import sys

fileName = sys.argv[1]
print(fileName)
df = pd.read_csv(fileName)
df.head()

trace = go.Scatter(x=df['time'], y=df['voltage'], mode='lines', name='time')
layout = go.Layout(title='Retrieved DAQ data', plot_bgcolor='rgb(230, 230, 230)')

fig = go.Figure(data=[trace], layout=layout)

plotly.offline.plot(fig, filename='data-plot.html')