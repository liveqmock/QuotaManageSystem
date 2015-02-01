<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.bstek.com/dorado/taglib-7.0" prefix="d" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<d:PageHeader/>
<style>
h1 {
	font-size: 14pt;
}
h3 {
	font-size: 11pt;
}
hr {
	border: none;
	border-bottom: 1px #77BDFD solid;
}
.place {
	border: 3px red solid;
	padding: 16px;
}
</style>
</head>
<body style="margin:24px; background:white url(images/summary-bg.png) no-repeat">

<h1>JSP模板</h1>

<h3>场景1 - renderTo模式:</h3>
<div id="place1" class="place"></div>

<h3>场景2 - renderOn模式:</h3>
<div id="place2" class="place"></div>

<h3>场景3 - renderOn容器模式:</h3>
<div id="place3" class="place">
这里的内容是通过HTML模板定义的！
<p>
<img src="images/dorado7-logo-big.png">
</p>
</div>

</body>