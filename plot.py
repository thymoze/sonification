import sys
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

keywords = ["c2h5oh", "c3h8", "c4h10", 
            "ch4", "co", "h2", "humidity", "nh3", 
            "no2", "pm10", "pm25", "pressure", 
            "temperatureBMP", "temperatureSHT"]
    
keywords = ['co', 'pm10', 'pm25']

if __name__ == "__main__":
    df = None

    with open(sys.argv[1], 'r') as file:
        df = pd.read_csv(file, parse_dates=['timestamp'])

    x = np.int64(df['timestamp'])
    
    for key in keywords:
        y = df[key]

        plt.figure(key)
        plt.plot(pd.to_datetime(x), y)

        z = np.polyfit(x, y, 3)
        p = np.poly1d(z)

        plt.plot(pd.to_datetime(x), p(x))

        plt.title(key)
        if key in ['pm10', 'pm25']:
            plt.ylim([0,50])
            plt.ylabel('$\mu g /m^3$')
            if key is 'pm10': 
                max = 40
            else: 
                max = 20
            plt.axhline(y=max, label="max. Jahresmittelwert")
    
    plt.show()
        



