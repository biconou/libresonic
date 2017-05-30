<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="iso-8859-1" %>

<html>
<head>
    <%@ include file="head.jsp" %>
    <%@ include file="jquery.jsp" %>
</head>

<script language"javascript">

$(document).ready(function() {

  $.get( "rest/ping.view?v=1.14.0&c=web&f=json", function( data ) {
    console.dir(data);
    $(".result").html(data['subsonic-response'].version);
  });

});


</script>

<body class="mainframe bgcolor1" style="padding-bottom:0.5em">


Ping ... Ping ...

<div class="result"></div>

</body>
</html>
