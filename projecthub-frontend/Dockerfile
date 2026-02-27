FROM nginx:alpine

RUN rm -rf /usr/share/nginx/html/*

COPY index.html    /usr/share/nginx/html/
COPY project.html  /usr/share/nginx/html/
COPY login.html    /usr/share/nginx/html/
COPY register.html /usr/share/nginx/html/
COPY style.css     /usr/share/nginx/html/
COPY app.js        /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80