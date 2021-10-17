function printStatus (requestParams, response, context, ee, next) {
    console.log(`Printing status for request to ${requestParams.url}`)
    console.log("Request headers are: ", requestParams.headers)
    console.log(`Received response: ${response.body}`)
    return next();
}

//https://stackoverflow.com/questions/58610133/how-to-capture-an-attribute-from-a-random-json-index-in-serverless-artillery
function setReservationTimeSlot(context, events, done) {
    const randomIndex = Math.round(Math.random() * 5);//context.vars.resources.length
    context.vars.reservationTimeSlot = context.vars.resources[randomIndex];

    //console.log(context.vars.resources[-1]);
    return done();
}

module.exports = {
    printStatus: printStatus,
    setReservationTimeSlot: setReservationTimeSlot
}