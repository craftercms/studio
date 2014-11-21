try {
    model.result = contentItemIdService.next();
} catch (e) {
     status.code = 500;
     status.message = "An error occured while retrieving the next id. " + e.message;
     status.redirect = true;
}
