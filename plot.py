import sys
import sqlite3
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt


keywords = {
    # KEY:            (NAME,                EINHEIT,       SKALA,     MAX )
    'pm10':           ('Feinstaub 10',      '$\mu g/m^3$', [0, 50],   40  ),
    'pm25':           ('Feinstaub 2.5',     '$\mu g/m^3$', [0, 50],   20  ),
    'co':             ('Kohlenmonoxid',     'ppm',         [0, 2],    None),
    'no2':            ('Stickstoffdioxid',  'ppm',         [0.05, 2], 0.5 ),
    'c2h5oh':         ('Ethanol',           'ppm',         [0, 50],   40  ),
    'c3h8':           ('Propan',            'ppm',         [0, 50],   40  ),
    'c4h10':          ('Isobutan',          'ppm',         [0, 50],   40  ),
    'ch4':            ('Methan',            'ppm',         [0, 50],   40  ),
    'nh3':            ('Ammoniak',          'ppm',         [1, 50],   20  ),
    'h2':             ('Wasserstoff',       'ppm',         [0, 1],    0.55),
    'humidity':       ('Luftfeuchtigkeit',  'ppm',         [0, 50],   None),
    'pressure':       ('Luftdruck',         'ppm',         [0, 50],   None),
    'temperatureBMP': ('Temperatur (BMP)',  '°C',          [0, 40],   None),
    'temperatureSHT': ('Temperatur (SHT)',  '°C',          [0, 40],   None),
}

if __name__ == '__main__':
    if len(sys.argv) < 2 or len(sys.argv) > 3 or sys.argv[1] in ['-h', '--help', '-?']:
        print('''USAGE: 
        python plot.py PATH_TO_CSV.csv
        python plot.py PATH_TO_DATABASE.db DATASET_INDEX(default=1)
        ''')
        exit(1)

    if sys.argv[1].endswith('.csv'):
        with open(sys.argv[1], 'r') as file:
            df = pd.read_csv(file, parse_dates=['timestamp'])
    else:
        con = sqlite3.connect(sys.argv[1])
        try:
            id = int(sys.argv[2])
        except Exception:
            id = 1

        try:
            df = pd.read_sql("SELECT * FROM SensorData WHERE dataSetId = %d" % id, con, parse_dates=['timestamp'])
        except Exception:
            df = pd.read_sql("SELECT * FROM SensorData", con, parse_dates=['timestamp'])
        if df.empty:
            print("Selected DataSet is empty/ does not exist!")
            exit(0)

    # convert timestamp to int so we can do calculations with it
    x = np.int64(df['timestamp'])
    
    for key, value in keywords.items():
        print(key)
        y = df[key]
        plt.figure(value[0])

        # Plot values
        plt.plot(pd.to_datetime(x), y)

        # Calculate and plot trendline
        z = np.polyfit(x, y, deg=5)
        p = np.poly1d(z)
        plt.plot(pd.to_datetime(x), p(x))

        plt.title(value[0])
        plt.xlabel('Zeit')
        plt.ylabel(value[1])
        plt.ylim(0)
        if value[3] is not None:
            plt.axhline(y=value[3])
    
    plt.show()
        



