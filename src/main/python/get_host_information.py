import pika
import os
import psutil
import platform
from hurry.filesize import size
from time import sleep
import time

if __name__ == '__main__':
    connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
    channel = connection.channel()

    while True:
        # More metrics at: https://github.com/giampaolo/psutil
        channel.basic_publish(exchange='', routing_key='total-memory-queue', body=str(size(psutil.virtual_memory()[0])))
        channel.basic_publish(exchange='', routing_key='used-memory-queue', body=str(size(psutil.virtual_memory()[3])))
        channel.basic_publish(exchange='', routing_key='available-memory-queue', body=str(size(psutil.virtual_memory()[1])))
        channel.basic_publish(exchange='', routing_key='platform-queue', body=str(platform.platform()))
        channel.basic_publish(exchange='', routing_key='cpu-used-queue', body=str(psutil.cpu_percent()))
        channel.basic_publish(exchange='', routing_key='cpu-cores-queue', body=str(psutil.cpu_count()))
        channel.basic_publish(exchange='', routing_key='cpu-frequency-queue', body=str(int(psutil.cpu_freq()[0])) + " MHz")
        channel.basic_publish(exchange='', routing_key='boot-time-queue', body=str(int(time.time() - psutil.boot_time())) + " seconds")

        sleep(1)