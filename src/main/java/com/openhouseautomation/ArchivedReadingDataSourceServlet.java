package com.openhouseautomation;

import static com.openhouseautomation.OfyService.ofy;

import com.openhouseautomation.model.ReadingHistory;
import com.openhouseautomation.model.Sensor;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.Query;
import com.ibm.icu.util.GregorianCalendar;
// because import java.util.GregorianCalendar gives a type mismatch (wtf?)
import com.ibm.icu.util.TimeZone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dras
 */
public class ArchivedReadingDataSourceServlet extends DataSourceServlet {
  private static final long serialVersionUID = 1L;
  private static final Logger log = Logger.getLogger(ArchivedReadingDataSourceServlet.class
      .getName());

  // TODO: only in place for testing, not for production
  @Override
  protected boolean isRestrictedAccessMode() {
    return false;
  }

  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest request) {

    // Create a data table,
    DataTable data = new DataTable();
    ArrayList cd = new ArrayList();
    try {
      // Fill the data table.
      cd.add(new ColumnDescription("date", ValueType.DATETIME, "Date"));
      Sensor sensor =
          ofy().load().type(Sensor.class).id(Long.parseLong(request.getParameter("id"))).now();
      log.log(Level.WARNING, "sensor={0}", sensor);
      if (sensor.getType() == Sensor.Type.TEMPERATURE || sensor.getType() == Sensor.Type.HUMIDITY) {
        cd.add(new ColumnDescription("high", ValueType.NUMBER, "High"));
        cd.add(new ColumnDescription("low", ValueType.NUMBER, "Low"));
      } else if (sensor.getType() == Sensor.Type.LIGHT) {
        cd.add(new ColumnDescription("nonzeroavg", ValueType.NUMBER, "Total"));
      } else if (sensor.getType() == Sensor.Type.WINDSPEED) {
        cd.add(new ColumnDescription("high", ValueType.NUMBER, "High"));
      }
      data.addColumns(cd);

      List<ReadingHistory> readings =
          ofy().load().type(ReadingHistory.class).ancestor(sensor).list();
      GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
      // without a timezone of GMT, you will get:
      // can't create DateTimeValue from GregorianCalendar that is not GMT.
      // and if you want your graph in a TZ other than GMT? Nope.
      for (ReadingHistory reading : readings) {
        cal.setTime(new Date(reading.getTimestamp().getTime()));
        if (sensor.getType() == Sensor.Type.TEMPERATURE || sensor.getType() == Sensor.Type.HUMIDITY) {
          data.addRowFromValues(cal, new Double(reading.getHigh()), new Double(reading.getLow()));
        } else if (sensor.getType() == Sensor.Type.LIGHT) {
          data.addRowFromValues(cal, new Double(reading.getAverage()));
        } else if (sensor.getType() == Sensor.Type.WINDSPEED) {
          data.addRowFromValues(cal, new Double(reading.getHigh()));
        }
      }
    } catch (final TypeMismatchException e) {
      log.log(Level.SEVERE, e.toString(), e);
    }
    return data;
  }
}
