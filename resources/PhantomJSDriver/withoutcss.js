var page = this;
page.onResourceRequested = function(requestData, request) {
    if ((/http:\/\/.+?\.css/gi).test(requestData['url']) || requestData['Content-Type'] == 'text/css'
    || (/http:\/\/.+?\.png/gi).test(requestData['url']) || (/http:\/\/.+?\.jpg/gi).test(requestData['url'])) {
        request.abort();
    }
};