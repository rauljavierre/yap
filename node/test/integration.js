const chai = require('chai');
const chaiHttp = require('chai-http');
const expect = require('chai').expect;
const url = "http://localhost"
const sleep = require('sleep');

chai.use(chaiHttp);

describe('Integration testing', () => {

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
        sleep.sleep(2)
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


});
