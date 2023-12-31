package satellite.access.tools.structures;

import java.util.ArrayList;
import java.util.List;

public class Interval extends Event {

    private long start;
    private long end;
    private long duration;
    private long timeSinceLastContact;
    private List<Integer> fromAssets = new ArrayList<>();
    private List<Integer> toAssets = new ArrayList<>();
    private double metric;

    public Interval(Interval interval) {
        this.start = interval.getStart();
        this.end = interval.getEnd();
        this.duration = end - start;
        fromAssets.addAll(interval.getFromAssets());
        toAssets.addAll(interval.getToAssets());
    }

    public Interval(long start, long end, Integer from, Integer to) {
        this.start = start;
        this.end = end;
        this.duration = end - start;
        fromAssets.add(from);
        toAssets.add(to);
    }

    public Interval(long start, long end, List<Integer> fromAssets, List<Integer> toAssets) {
        this.start = start;
        this.end = end;
        this.fromAssets = new ArrayList<>(fromAssets);
        this.toAssets = new ArrayList<>(toAssets);
        this.duration = end - start;
    }

    public Interval(long start, long end) {
        this.start = start;
        this.end = end;
        this.duration = end - start;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getDuration() {
        return duration;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public void setTimeSinceLastContact(long timeSinceLastContact) {
        this.timeSinceLastContact = timeSinceLastContact;
    }

    public long getTimeSinceLastContact() {
        return timeSinceLastContact;
    }

    public List<Integer> getFromAssets() {
        return fromAssets;
    }

    public List<Integer> getToAssets() {
        return toAssets;
    }

    public Integer getFirstFrom() {
        return fromAssets.get(0);
    }

    public Integer getFirstTo() {
        return toAssets.get(0);
    }

    public void setFirstTo(int id) {
        if (!toAssets.isEmpty()) {
            toAssets.set(0, id);
        } else {
            toAssets.add(0, id);
        }
    }

    public void setFirstFrom(int id) {
        if (!fromAssets.isEmpty()) {
            fromAssets.set(0, id);
        } else {
            fromAssets.add(0, id);
        }
    }

    public void addFrom(Integer indexFrom) {
        this.fromAssets.add(indexFrom);
    }

    public void removeFrom(Integer indexFrom) {
        this.fromAssets.remove(indexFrom);
    }

    public void addTo(Integer indexTo) {
        this.toAssets.add(indexTo);
    }

    public void removeTo(Integer indexTo) {
        this.toAssets.remove(indexTo);
    }

    public double getMetric() {
        return metric;
    }

    public void setMetric(double metric) {
        this.metric = metric;
    }

    @Override
    public String toString() {
        return start + "," + end + "," + duration;
    }
}