package com.openhouseautomation.mapreduce;

import com.google.appengine.tools.mapreduce.Reducer;
import com.google.appengine.tools.mapreduce.ReducerInput;
import static com.openhouseautomation.OfyService.ofy;
import com.openhouseautomation.model.ReadingHistory;
import com.openhouseautomation.model.Sensor;
import com.googlecode.objectify.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dras
 */
public class ReadingReducer extends Reducer<String, String, ReadingHistory> {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(ReadingReducer.class.getName());

  @Override
  public void reduce(String key, ReducerInput<String> values) {
    LOG.log(Level.WARNING, "reducing: {0}", key);
    if (key.startsWith("TEMPERATURE") || key.startsWith("HUMIDITY")) {
      reduceHighLow(key, values);
    } else if (key.startsWith("LIGHT")) {
      reduceTotal(key, values);
    } else if (key.startsWith("WINDSPEED")) {
      reduceHigh(key, values);
    }
  }

  public void reduceHighLow(String key, ReducerInput<String> values) {
    String high = "-9999";
    String low = "9999";
    Float val = 0f;
    while (values.hasNext()) {
      String value = values.next();
      val = Float.parseFloat(value);
      if (val > Float.parseFloat(high)) {
        high = value;
      }
      if (val < Float.parseFloat(low)) {
        low = value;
      }
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setHigh(high);
    rhist.setLow(low);
    rhist.setTimestamp(convertStringDate(readingdate));
    deduplicateStore(rhist);
    // emit(rhist); // needs to emit an entity
  }

  public void reduceAvgNonZero(String key, ReducerInput<String> values) {
    Float totalval = 0f;
    Float avgval = 0f;
    int readings = 0;
    while (values.hasNext()) {
      String value = values.next();
      float fval = Float.parseFloat(value);
      if (fval > 0) {
        totalval += fval;
        readings++;
      }
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setAverage(Float.toString(totalval / readings));
    rhist.setTimestamp(convertStringDate(readingdate));
    deduplicateStore(rhist);
    // emit(rhist);
  }

  public void reduceTotal(String key, ReducerInput<String> values) {
    Float totalval = 0f;
    while (values.hasNext()) {
      String value = values.next();
      float fval = Float.parseFloat(value);
      totalval += fval;
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setAverage(Float.toString(totalval));
    rhist.setTimestamp(convertStringDate(readingdate));
    deduplicateStore(rhist);
    // emit(rhist);
  }

  public void reduceHigh(String key, ReducerInput<String> values) {
    String high = "-9999";
    Float val = 0f;
    while (values.hasNext()) {
      String value = values.next();
      val = Float.parseFloat(value);
      if (val > Float.parseFloat(high)) {
        high = value;
      }
    }
    StringTokenizer st1 = new StringTokenizer(key, ":");
    String sensortype = st1.nextToken();
    String sensorid = st1.nextToken();
    String readingdate = st1.nextToken();
    ReadingHistory rhist = new ReadingHistory();
    rhist.setSensor(Key.create(Sensor.class, Long.parseLong(sensorid)));
    rhist.setHigh(high);
    rhist.setTimestamp(convertStringDate(readingdate));
    deduplicateStore(rhist);
    // emit(rhist);
  }

  public Date convertStringDate(String s) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date d;
    try {
      d = dateFormat.parse(s);
    } catch (ParseException pe) {
      return new Date(0L);
    }
    return d;
  }

  public void deduplicateStore(ReadingHistory rh) {
    List<ReadingHistory> stored = ofy().load().type(ReadingHistory.class).ancestor(rh.getSensor()).filter("timestamp = ", rh.getTimestamp().getTime()).list();
    // delete the old entry/entries for this sensor/date combo
    for (ReadingHistory rhlist : stored) {
      LOG.log(Level.WARNING, "deleting: {0}", rhlist);
      ofy().delete().entity(rhlist);
    }
    // and save the new entity
    LOG.log(Level.WARNING, "adding: {0}", rh);
    ofy().save().entity(rh).now();
  }
}
