# YAP PLATFORM (URL SHORTENER) - Development

<img src='https://github.com/rauljavierre/yap/blob/master/logo/logo-2150297.png' width='400'>

## SEE THE API DOCUMENTATION ON SWAGGER
Todo...

<br>

## HOW TO BUILD AND LAUNCH THE ENTIRE APPLICATION


### Install git

<code>sudo apt-get install git</code>

### Clone this repository in your machine

<code>git clone https://github.com/rauljavierre/yap/</code>

### Install docker and docker-compose
```
sudo apt install docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo apt-get install docker-compose
```

### Install gradle

<code>sudo snap install gradle --classic</code>

### Install python3 and dependencies
```
sudo apt-get install python3
sudo apt install python3-pip
sudo apt-get install python3-dev
pip3 install pika
pip3 install psutil
```

### Run the server
```
cd ./docker
sudo bash up.sh
```

<br>


## HOW TO CONNECT TO THE HOST VIA SSH

First of all, you need to generate a pair of keys. Then, you are able to send the public key to us in order to give you access to the host.

### Add the private key to your SSH connections and login

```
chmod 400 <private-key>
ssh-add <private-key>
ssh yapsh.tk
```
