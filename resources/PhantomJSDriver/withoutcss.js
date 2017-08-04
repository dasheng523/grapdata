var page = this;
page.onResourceRequested = function(requestData, request) {
    if ((/http:\/\/.+?\.css/gi).test(requestData['url'])
        || requestData['Content-Type'] == 'text/css'
        || (/https:\/\/.+?.css/gi).test(requestData['url'])
        || (/http:\/\/.+?\.png/gi).test(requestData['url'])
        || (/https:\/\/.+?\.png/gi).test(requestData['url'])
        || (/http:\/\/.+?\.jpg/gi).test(requestData['url'])
        || (/https:\/\/.+?\.jpg/gi).test(requestData['url'])
        || requestData['url'].indexOf("//localhost")>0) {
        request.abort();
    }
};
