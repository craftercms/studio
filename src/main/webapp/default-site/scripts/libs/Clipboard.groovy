package scripts.libs

import groovy.util.logging.Log

@Log
class Clipboard {
    public static copy(site, items, cut, deep) {
        log.info "[Clipboard][Copy] copy called for " + site + " cut:" + cut + " deep:" + deep;
        log.info "[Clipboard][Copy] items " + items;
    }
}