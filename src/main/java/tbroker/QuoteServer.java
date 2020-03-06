package tbroker;

import java.util.*;

public class QuoteServer implements Quote {
    public static QuoteServer instance;

    public static QuoteServer getInstance() {
        if (instance == null)
            instance = new QuoteServer();
        return instance;
    }

    private class QuoteAggregator extends Util implements QuoteListener {
        LinkedList<QuoteListener> listeners;
        Date ts, lts;

        QuoteAggregator(String sym) {
            listeners = new LinkedList<QuoteListener>();
            for (Quote quote : src) {
                log("quote: " + quote.getClass().getName());
                if (quote.support(sym)) {
                    log("support:" + sym);
                    quote.bind(sym, this);
                } else {
                    log("not support:" + sym);
                }
            }
        }

        public void dayOpen(Date date) {
            for (QuoteListener l : listeners)
                l.dayOpen(date);
        }

        public void tick(Tick tick) {
            Date d = tick.getDate();
            lts = d;
            if (ts != null && ts.getTime() > d.getTime()) {
                return;
            }
            ts = d;

            for (QuoteListener l : listeners) {
                try {
                    l.tick(tick);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void dayClose() {
            for (QuoteListener l : listeners)
                l.dayClose();
        }

        public String toString() {
            return formatL(ts) + "," + formatL(lts);
        }
    }

    private LinkedList<Quote> src;

    private Hashtable<String, QuoteAggregator> map;

    private QuoteServer() {
        src = new LinkedList<Quote>();
        map = new Hashtable<String, QuoteAggregator>();
    }

    public void add(Quote l) {
        src.add(l);
    }

    public int getNumQuotes() {
        return src.size();
    }

    public void login(String dummy) {
    }

    public boolean support(String sym) {
        for (Quote quote : src) {
            if (quote.support(sym))
                return true;
        }
        return false;
    }

    public synchronized void bind(String sym, QuoteListener l) {
        QuoteAggregator aggregator = map.get(sym);
        if (aggregator == null) {
            aggregator = new QuoteAggregator(sym);
            map.put(sym, aggregator);
        }
        if (!aggregator.listeners.contains(l)) {
            aggregator.listeners.add(l);
        }
    }

    public String toString() {
        String s = "";
        for (String sym : map.keySet()) {
            QuoteAggregator agg = map.get(sym);
            s += sym + "," + agg.toString() + "\r\n";
        }
        return s;
    }
}
