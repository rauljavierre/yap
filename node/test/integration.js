const chai = require('chai');
const chaiHttp = require('chai-http');
const expect = require('chai').expect;
const sleep = require('sleep');
const WebSocket = require('ws');
const { exec } = require('child_process');

const url = "http://localhost"
const socketUrl = "ws://localhost/csv"

chai.use(chaiHttp);


describe('Integration testing', () => {

    it('Should do /check with a valid url', (done) => {
        chai.request(url)
            .get('/check?url=' + encodeURI('http://yapsh.tk/'))
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('isValid').to.be.equal("URL is OK");
                done();
            })
    });

    it('Should do /check with an empty url', (done) => {
        chai.request(url)
            .get('/check?url=')
            .end((err, res) => {
                expect(res).to.have.status(400);
                done();
            })
    });

    it('Should do /check with an null url', (done) => {
        chai.request(url)
            .get('/check')
            .send({})
            .end((err, res) => {
                expect(res).to.have.status(400);
                done();
            })
    });

    it('Should do /check with a malformed url', (done) => {
        chai.request(url)
            .get('/check?url=' + encodeURI('httdddp://yapsh.tk/'))
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('isValid').to.be.equal("URL is malformed");
                done();
            })
    });

    it('Should do /actuator/info and return 200 and all entities are null', (done) => {
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal(null);
                expect(res.body).to.have.property('QRs').to.be.equal(null);
                expect(res.body).to.have.property('CSVs').to.be.equal(null);
                expect(res.body).to.have.property('timestamp');

                done();
            })
    });

    let hash = undefined;
    it('Should do /link with a reachable URL and requesting a QR and return 201', (done) => {
        chai.request(url)
            .post('/link')
            .send(
                {
                    'url': 'http://yapsh.tk/',
                    'generateQR': 'true'
                }
            )
            .end((err, res) => {
                expect(res).to.have.status(201);
                expect(res.body).to.have.property('url');
                expect(res.body).to.have.property('qr');
                expect(res.header).to.have.property('location');

                hash = res.body.url.split('/').slice(-1).pop();    // get only the hash

                done();
            })
    });

    it('Should do /actuator/info and return 200 and QRs should be 1 and URLs should be 1', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal('1');
                expect(res.body).to.have.property('QRs').to.be.equal('1');
                expect(res.body).to.have.property('CSVs').to.be.equal(null);
                expect(res.body).to.have.property('timestamp');

                done();
            })
    });

    it('Should do /link with a reachable URL without requesting a QR and return 201', (done) => {
        chai.request(url)
            .post('/link')
            .send(
                {
                    'url': 'http://yapsh.tk/',
                    'generateQR': 'false'
                }
            )
            .end((err, res) => {
                expect(res).to.have.status(201);
                expect(res.body).to.have.property('url');
                expect(res.body).not.to.have.property('qr');
                expect(res.header).to.have.property('location');

                hash = res.body.url.split('/').slice(-1).pop();    // get only the hash

                done();
            })
    });


    let malformedHash = undefined;
    it('Should do /link with a malformed URL and return 201', (done) => {
        chai.request(url)
            .post('/link')
            .send(
                {
                    'url': 'httpd://yapsh.tk/',
                    'generateQR': 'true'
                }
            )
            .end((err, res) => {
                expect(res).to.have.status(201);
                expect(res.body).to.have.property('url');
                expect(res.body).to.have.property('qr');

                malformedHash = res.body.url.split('/').slice(-1).pop();    // get only the hash

                done();
            })
    });

    it('Should do /actuator/info and return 200 and QRs should be 2 and URLs should be 1', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal('1');
                expect(res.body).to.have.property('QRs').to.be.equal('2');
                expect(res.body).to.have.property('CSVs').to.be.equal(null);
                expect(res.body).to.have.property('timestamp');

                done();
            })
    });

    let notReachableHash = undefined;
    it('Should do /link with a not reachable URL and return 201', (done) => {
        chai.request(url)
            .post('/link')
            .send(
                {
                    'url': 'http://notreachable.org.tk.es/',
                    'generateQR': 'true'
                }
            )
            .end((err, res) => {
                expect(res).to.have.status(201);
                expect(res.body).to.have.property('url');
                expect(res.body).to.have.property('qr');

                notReachableHash = res.body.url.split('/').slice(-1).pop();    // get only the hash

                done();
            })
    });

    it('Should do /actuator/info and return 200 and QRs should be 3 and URLs should be 1', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal('1');
                expect(res.body).to.have.property('QRs').to.be.equal('3');
                expect(res.body).to.have.property('CSVs').to.be.equal(null);
                expect(res.body).to.have.property('timestamp');

                done();
            })
    });


    it('Should do a redirect with that URL and return 200', (done) => {
        sleep.sleep(4)
        chai.request(url)
            .get("/" + hash)
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.redirects[0]).to.contains('http://yapsh.tk/')
                done();
            })
    });

    it('Should NOT do a redirect with a malformed URL and return 406', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get("/" + malformedHash)
            .end((err, res) => {
                expect(res).to.have.status(406);
                done();
            })
    });

    it('Should NOT do a redirect with a not reachable URL and return 406', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get("/" + notReachableHash)
            .end((err, res) => {
                expect(res).to.have.status(406);
                done();
            })
    });

    it('Should NOT do a redirect with a not requested URL and return 404', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get("/" + "notRequestHash")
            .end((err, res) => {
                expect(res).to.have.status(404);
                done();
            })
    });

    it('Should do /link with a null URL and return 400', (done) => {
        chai.request(url)
            .post('/link')
            .send(
                {}
            )
            .end((err, res) => {
                expect(res).to.have.status(400);
                done();
            })
    });

    it('Should do /actuator/info and return 200 and QRs should be 3 and URLs should be 1', (done) => {
        sleep.sleep(2)
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal('1');
                expect(res.body).to.have.property('QRs').to.be.equal('3');
                expect(res.body).to.have.property('CSVs').to.be.equal(null);
                expect(res.body).to.have.property('timestamp');

                done();
            })
    });

    it('Should do /qr/{hash} with a hash that doesn\'t exist in database and return 404', (done) => {
        chai.request(url)
            .get('/qr/1')
            .end((err, res) => {
                expect(res).to.have.status(404);
                expect(res.body).to.have.property('error').to.be.equal('URL was not requested with /link');
                done();
        })
    });

    let slowHash = undefined;
    it('Should do /link with a not slow reachable URL and return 201', (done) => {
        chai.request(url)
        .post('/link')
        .send(
            {
                'url': 'https://techcrunch.com/',
                'generateQR': 'true'
            }
        ).end((err, res) => {
             expect(res).to.have.status(201);
             slowHash = res.body.url.split('/').slice(-1).pop();    // get only the hash
             done();
         })
    });


    it('Should do /qr/{hash} with a hash of a url that haven\'t been validated yet in database and return 404', (done) => {
        sleep.msleep(300)
        chai.request(url)
        .get('/qr/' + slowHash )
        .end((err, res) => {
            expect(res).to.have.status(404);
            expect(res.body).to.have.property('error').to.not.equal("URL was not requested with /link");
            done();
        })
    });


    it('Should do /actuator/info and return 200 and QRs should be 4 and URLs should be 2', (done) => {
        sleep.sleep(3)
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal('2');
                expect(res.body).to.have.property('QRs').to.be.equal('4');
                expect(res.body).to.have.property('CSVs').to.be.equal(null);
                expect(res.body).to.have.property('timestamp');

                done();
            })
    });

    it('Should do /qr/{hash} with a hash that exist in database and returns 200', (done) => {
            sleep.msleep(2000)
            chai.request(url)
            .get('/qr/' + hash )
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.header).to.have.property('cache-control').to.be.equal("CacheControl [max-age=31536000, must-revalidate, no-transform]");
                done();
            })
        });

    let client1, client2, client3;

    it('Should send a long URL via WebSockets and return a valid short URL', (done) => {
        let testingUrl = "https://google.es/";
        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            let hash = responseShortUrl.split("/")[3];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;
            client1.close(1000, "WebSocket Closed");
            sleep.sleep(3);
            chai.request(url)
                .get("/" + hash)
                .end((err, res) => {
                    expect(res).to.have.status(200);
                    expect(res.redirects[0]).to.contains(testingUrl);
                    done();
                })
        };
        client1.onopen = function(e) {
            client1.send(testingUrl);
        }
    });

    it('Should send a malformed URL via WebSockets and return an error response', (done) => {
        let testingUrl = "gugelpuntocom";
        client2 = new WebSocket(socketUrl);
        client2.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal("URL is malformed");
            expect(responseShortUrl).to.be.empty;
            client2.close(1000, "WebSocket Closed");
            done();
        };
        client2.onopen = function(e) {
            client2.send(testingUrl);
        }
    });

    it('Should send a non reachable URL via WebSockets and return an error response', (done) => {
        let testingUrl = "https://urlquenoesalcanzable.com";
        client3 = new WebSocket(socketUrl);
        client3.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal("URL not reachable");
            expect(responseShortUrl).to.be.empty;
            client3.close(1000, "WebSocket Closed");
            done();
        };
        client3.onopen = function(e) {
            client3.send(testingUrl);
        }
    });

    it('Should get the metric http server requests of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/http.server.requests')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("http.server.requests");
                done();
            })
    });

    it('Should get the metric jvm memory committed of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/jvm.memory.committed')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("jvm.memory.committed");
                done();
            })
    });

    it('Should get the metric jvm memory max of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/jvm.memory.max')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("jvm.memory.max");
                done();
            })
    });

    it('Should get the metric jvm memory used of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/jvm.memory.used')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("jvm.memory.used");
                done();
            })
    });

    it('Should get the metric jvm threads live of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/jvm.threads.live')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("jvm.threads.live");
                done();
            })
    });

    it('Should get the metric process cpu usage of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/process.cpu.usage')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("process.cpu.usage");
                done();
            })
    });

    it('Should get the metric process uptime of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/process.uptime')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("process.uptime");
                done();
            })
    });

    it('Should get the metric system cpu usage of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/system.cpu.usage')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("system.cpu.usage");
                done();
            })
    });

    it('Should get the metric system load average 1m of a CSV worker', (done) => {
        chai.request(url)
            .get('/actuator/metrics/system.load.average.1m')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('name').to.be.equal("system.load.average.1m");
                done();
            })
    });

    it('Should do /link with a reachable URL without specifying property "generateQR" and return 201', (done) => {
        chai.request(url)
            .post('/link')
            .send(
                {
                    'url': 'http://yapsh.tk/'
                }
            )
            .end((err, res) => {
                expect(res).to.have.status(201);
                expect(res.body).to.have.property('url');
                expect(res.body).not.to.have.property('qr');
                expect(res.header).to.have.property('location');

                hash = res.body.url.split('/').slice(-1).pop();    // get only the hash

                done();
            })
    });

    it('Should scale the CSV worker microservice (2)', (done) => {
        exec('sudo docker service scale yap_csvsworker=2', (err, stdout, stderr) => {
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_csvsworker | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(2);
                        done();
                    }
                });
            }
        });
    });

    it('Should send a long URL via WebSockets and return a valid short URL with 2 workers', (done) => {
        let testingUrl = "https://google.es/";
        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            let hash = responseShortUrl.split("/")[3];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;
            client1.close(1000, "WebSocket Closed");
            sleep.sleep(3);
            chai.request(url)
                .get("/" + hash)
                .end((err, res) => {
                    expect(res).to.have.status(200);
                    expect(res.redirects[0]).to.contains(testingUrl);
                    done();
                })
        };
        client1.onopen = function(e) {
            client1.send(testingUrl);
        }
    });

    it('Should send a malformed URL via WebSockets and return an error response with 2 workers', (done) => {
        let testingUrl = "gugelpuntocom";
        client2 = new WebSocket(socketUrl);
        client2.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal("URL is malformed");
            expect(responseShortUrl).to.be.empty;
            client2.close(1000, "WebSocket Closed");
            done();
        };
        client2.onopen = function(e) {
            client2.send(testingUrl);
        }
    });

    it('Should send a non reachable URL via WebSockets and return an error response with 2 workers', (done) => {
        let testingUrl = "https://urlquenoesalcanzable.com";
        client3 = new WebSocket(socketUrl);
        client3.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal("URL not reachable");
            expect(responseShortUrl).to.be.empty;
            client3.close(1000, "WebSocket Closed");
            done();
        };
        client3.onopen = function(e) {
            client3.send(testingUrl);
        }
    });

    it('Should send 1000 long URLs via WebSockets and return all the short URLs with 2 workers', (done) => {
        let testingUrl = "https://google.es/";
        let received = 0;

        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            received += 1
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;

            if (received === 1000) {
                client1.close(1000, "WebSocket Closed");
                done()
            }
        };

        client1.onopen = function(e) {
            for (i = 0; i < 1000; i++) {
                client1.send(testingUrl);
            }
        }
    });

    it('Should scale the CSV worker microservice (2)', (done) => {
        exec('sudo docker service scale yap_csvsworker=2', (err, stdout, stderr) => {
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_csvsworker | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(2);
                        done();
                    }
                });
            }
        });
    });

    it('Should send a long URL via WebSockets and return a valid short URL with 2 workers', (done) => {
        let testingUrl = "https://google.es/";
        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            let hash = responseShortUrl.split("/")[3];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;
            client1.close(1000, "WebSocket Closed");
            sleep.sleep(3);
            chai.request(url)
                .get("/" + hash)
                .end((err, res) => {
                    expect(res).to.have.status(200);
                    expect(res.redirects[0]).to.contains(testingUrl);
                    done();
                })
        };
        client1.onopen = function(e) {
            client1.send(testingUrl);
        }
    });

    it('Should send a malformed URL via WebSockets and return an error response with 2 workers', (done) => {
        let testingUrl = "gugelpuntocom";
        client2 = new WebSocket(socketUrl);
        client2.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal("URL is malformed");
            expect(responseShortUrl).to.be.empty;
            client2.close(1000, "WebSocket Closed");
            done();
        };
        client2.onopen = function(e) {
            client2.send(testingUrl);
        }
    });

    it('Should send a non reachable URL via WebSockets and return an error response with 2 workers', (done) => {
        let testingUrl = "https://urlquenoesalcanzable.com";
        client3 = new WebSocket(socketUrl);
        client3.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal("URL not reachable");
            expect(responseShortUrl).to.be.empty;
            client3.close(1000, "WebSocket Closed");
            done();
        };
        client3.onopen = function(e) {
            client3.send(testingUrl);
        }
    });

    it('Should send 1000 long URLs via WebSockets and return all the short URLs with 2 workers', (done) => {
        let testingUrl = "https://google.es/";
        let received = 0;

        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            received += 1
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;

            if (received === 1000) {
                client1.close(1000, "WebSocket Closed");
                done()
            }
        };

        client1.onopen = function(e) {
            for (i = 0; i < 1000; i++) {
                client1.send(testingUrl);
            }
        }
    });

    it('Should tear down all the Spring Boot Microservices', (done) => {
        exec('sudo docker service scale yap_csvsworker=0 && sudo docker service scale yap_urlsqrs=0 && sudo docker service scale yap_csvsmaster=0', (err, stdout, stderr) => {
            sleep.sleep(15)
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_urlsqrs | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(0);
                    }
                });
                exec('sudo docker ps -a | grep yap_csvsmaster | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(0);
                    }
                });
                exec('sudo docker ps -a | grep yap_csvsworker | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(0);
                    }
                });
                done();
            }
        });
    });

    it('Should tear up again all the Spring Boot Microservices', (done) => {
        exec('sudo docker service scale yap_csvsworker=1 && sudo docker service scale yap_csvsmaster=1 && sudo docker service scale yap_urlsqrs=1', (err, stdout, stderr) => {
            sleep.sleep(30)
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_urlsqrs | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(1);
                    }
                });
                exec('sudo docker ps -a | grep yap_csvsmaster | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(1);
                    }
                });
                exec('sudo docker ps -a | grep yap_csvsworker | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(1);
                    }
                });
                done();
            }
        });
    });

    it('Should NOT had lost the information of the database and the system should be operative again', (done) => {
        sleep.sleep(30)
        chai.request(url)
            .get('/actuator/info')
            .end((err, res) => {
                expect(res).to.have.status(200);
                expect(res.body).to.have.property('URLs').to.be.equal('3');
                expect(res.body).to.have.property('QRs').to.be.equal('4');
                expect(res.body).to.have.property('CSVs').to.be.equal('11');
                expect(res.body).to.have.property('timestamp');
                done();
            })
    });

    it('Should send 1000 long URLs via WebSockets and return all the short URLs with 1 worker after the reset of the microservices', (done) => {
        let testingUrl = "https://google.es/";
        let received = 0;

        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            received += 1
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;

            if (received === 1000) {
                client1.close(1000, "WebSocket Closed");
                done()
            }
        };

        client1.onopen = function(e) {
            for (i = 0; i < 1000; i++) {
                client1.send(testingUrl);
            }
        }
    });

    it('Should tear down the URLs and QRs microservice', (done) => {
        exec('sudo docker service scale yap_urlsqrs=0', (err, stdout, stderr) => {
            sleep.sleep(15)
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_urlsqrs | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(0);
                    }
                });
                done();
            }
        });
    });

    it('Should send a long URL via WebSockets and return a valid stored response without cascade failures', (done) => {
        let testingUrl = "https://google.es/";
        client1 = new WebSocket(socketUrl);
        client1.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseShortUrl = msg.split(",")[1];
            let responseStatus = msg.split(",")[2];
            let hash = responseShortUrl.split("/")[3];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.empty;
            expect(hash).not.to.be.empty;
            client1.close(1000, "WebSocket Closed");
            done();
        };
        client1.onopen = function(e) {
            client1.send(testingUrl);
        }
    });

    it('Should send a long URL via WebSockets and return a "try again" response without cascade failures', (done) => {
        let testingUrl = "https://google.com/";
        client2 = new WebSocket(socketUrl);
        client2.onmessage = function(event) {
            let msg = event.data;
            let responseLongUrl = msg.split(",")[0];
            let responseStatus = msg.split(",")[2];
            expect(responseLongUrl).to.equal(testingUrl);
            expect(responseStatus).to.be.equal('PleaseTryAgain');
            client2.close(1000, "WebSocket Closed");
            done();
        };
        client2.onopen = function(e) {
            client2.send(testingUrl);
        }
    });

    it('Should tear up the URLs and QRs microservice', (done) => {
        exec('sudo docker service scale yap_urlsqrs=1', (err, stdout, stderr) => {
            sleep.sleep(15)
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_urlsqrs | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(1);
                    }
                });
                done();
            }
        });
    });

    it('Should send 1000 different long URLs via WebSockets and return all the short URLs with 1 worker after the reset of the microservices', (done) => {
        let testingUrl = "https://www.instagram.com/";
        let received = 0;

        client1 = new WebSocket(socketUrl);
        client1.onmessage = function (event) {
            received += 1
            let msg = event.data;
            let responseStatus = msg.split(",")[2];
            expect(responseStatus).to.be.empty;

            if (received === 1000) {
                client1.close(1000, "WebSocket Closed");
                done()
            }
        };

        client1.onopen = function (e) {
            for (i = 0; i < 1000; i++) {
                client1.send(testingUrl + i);
            }
        }
    });

    it('Should scale the CSV worker microservice (2)', (done) => {
        exec('sudo docker service scale yap_csvsworker=2', (err, stdout, stderr) => {
            if (err) {
                console.error(err)
            }
            else {
                exec('sudo docker ps -a | grep yap_csvsworker | grep Up | wc -l', (err, stdout, stderr) => {
                    if (err) {
                        console.error(err)
                    }
                    else {
                        expect(parseInt(stdout)).to.be.equal(2);
                        sleep.sleep(10)
                        done();
                    }
                });
            }
        });
    });

    it('Should send 1000 long URLs via WebSockets and return all the short URLs with 2 workers after the reset of the microservices', (done) => {
        let testingUrl = "https://www.instagram.com/different";
        let received = 0;

        client1 = new WebSocket(socketUrl);
        client1.onmessage = function (event) {
            received += 1
            let msg = event.data;
            let responseStatus = msg.split(",")[2];
            expect(responseStatus).to.be.empty;

            if (received === 1000) {
                client1.close(1000, "WebSocket Closed");
                done()
            }
        };

        client1.onopen = function (e) {
            for (i = 0; i < 1000; i++) {
                client1.send(testingUrl + i);
            }
        }
    });
});