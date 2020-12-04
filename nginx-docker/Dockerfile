FROM nginx:latest

RUN mkdir -p /data/nginx/cache
RUN chown nginx:nginx /data/nginx/cache
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf