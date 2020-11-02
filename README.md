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
sudo apt install docker.io</code>
sudo systemctl start docker
sudo systemctl enable docker
sudo apt-get install docker-compose
```

### Install gradle

<code>sudo snap install gradle --classic</code>

### Install python3 and dependencies
```
sudo apt-get install python3
sudo apt-get install pip
sudo apt-get install python3-dev
pip install pika
pip install psutil
```

### Run the server
```
cd ./docker
sudo bash up.sh
```

<br>


## HOW TO CONNECT TO THE HOST VIA SSH

First of all, you need to get the private key (ask the developers if needed)

### Add the private key to your SSH connections and login with "ubuntu" user

```
chmod 400 ssh-key-2020-10-29.key
ssh-add ssh-key-2020-10-29.key
ssh ubuntu@yapsh.tk
```
