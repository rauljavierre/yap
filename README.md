# YAP PLATFORM (URL SHORTENER) - Development



## HOW TO BUILD AND LAUNCH THE ENTIRE APPLICATION


### Install git
```
sudo apt-get install git
```

### Clone this repository in your machine
```
git clone https://github.com/rauljavierre/yap/
```

### Install docker and docker-compose
```
sudo apt install docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo apt-get install docker-compose
```

### Install gradle
```
sudo snap install gradle --classic
```

### Run the server
```
sudo bash up.sh
```

## HOW TO CONNECT TO THE HOST VIA SSH

First of all, you need to generate a pair of keys. Then, you are able to send the public key to us in order to give you access to the host.

### Add the private key to your SSH connections and login
```
chmod 400 <private-key>
ssh-add <private-key>
ssh yapsh.tk
```
