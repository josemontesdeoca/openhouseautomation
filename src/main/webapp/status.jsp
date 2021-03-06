
<%@page import="com.openhouseautomation.Convutils"%>
<%@page import="com.google.appengine.api.datastore.QueryResultIterator"%>
<%@page import="com.googlecode.objectify.cmd.Query"%>
<%--<%@page import="static com.googlecode.objectify.ObjectifyService.ofy"%>DO NOT USE!--%>
<%@page import="static com.openhouseautomation.OfyService.ofy"%>
<%@page import="com.openhouseautomation.model.Sensor"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  long snaptime = System.currentTimeMillis(); // page timing
  Query<Sensor> query = ofy().load().type(Sensor.class);
  QueryResultIterator<Sensor> iterator = query.iterator();
%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>gAutoArd</title>

    <!-- Bootstrap core CSS -->
    <link href="/css/bootstrap.min.css" rel="stylesheet">

    <!-- gAutoArd core CSS -->
    <link href="/css/main.css" rel="stylesheet">

    <link rel="stylesheet" href="/css/jquery-ui.css">

    <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
    <style>
      #slider { margin: 10px; }
    </style>
  </head>
  <body>
    <div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
      <div class="container-fluid">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="/">gAutoArd</a>
        </div>
        <div class="navbar-collapse collapse">
          <ul class="nav navbar-nav navbar-right">
            <li><a href="https://code.google.com/p/gautoard/wiki/DesignConcepts">Help</a></li>
          </ul>
        </div>
      </div>
    </div>

    <div class="container-fluid">
      <div class="row">
        <div class="col-sm-3 col-md-2 sidebar">
          <ul class="nav nav-sidebar">
            <li>Overview</li>
            <li class="divider"></li>
            <li class="nav-header">Sensors</li>
            <li class="divider"></li>
            <li class="nav-header">Controllers</li>
            <li class="divider"></li>
            <li class="nav-header">Readings</li>
            <li class="active"><a href="/status.jsp">Current</a></li>
            <li><a href="/charts/weekly.html">Weekly</a></li>
            <li><a href="/charts/archived.html">Archived</a></li>
          </ul>
        </div>
        <div class="col-sm-9 col-sm-offset-3 col-md-10 col-md-offset-2 main">
          <h1>Status</h1>
          <table>
            <%
              while (iterator.hasNext()) {
                Sensor sens = (Sensor) iterator.next();
            %>
            <tr><td>
                <%= sens.getName()%>:
              </td><td>
                <%= sens.getLastReading()%>&nbsp;<%= sens.getUnit()%>
              </td><td>
                <%= Convutils.timeAgoToString(sens.getLastReadingDate().getTime() / 1000, 4 * 60 * 60)%>
              </td>
              </td>
            </tr>
            <% }%>
          </table>
        </div>
      </div>
    </div>

    <p>Page Generation: <%= (System.currentTimeMillis() - snaptime)%> ms</p>

    <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
    <!-- Include all compiled plugins (below), or include individual files as needed -->
    <script src="/js/bootstrap.min.js"></script>

    <script src="/js/jquery-ui.js"></script>

  </body>
</html>
