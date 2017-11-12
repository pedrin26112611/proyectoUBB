<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<html>
 
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Users List</title>

<link href="<c:url value='/static/css/app.css' />" rel="stylesheet"></link>
<link rel="stylesheet" type="text/css"
	href="//cdnjs.cloudflare.com/ajax/libs/font-awesome/4.2.0/css/font-awesome.css" />
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-alpha.6/css/bootstrap.min.css"
	integrity="sha384-rwoIResjU2yc3z8GV/NPeZWAv56rSmLldC3R/AZzGRnGxQQKnKkoFVhFQhNUwEyJ"
	crossorigin="anonymous">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/estilos.css">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/slider.css">

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/animate.css">
<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/secciones.css">

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/responsive.css">

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/font-awesome.min.css">

<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/login.css">
	<link rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/resources/css/footer-distributed-with-address-and-phones.css">
</head>
 
<body>

<%@include file="authheader.jsp"%>
<br><br><br><br><br><br><br><br>
<div class="content-wrapper">

  <section
				class="container">
			<caption>
				<p>
					<a href="<c:url value="newVinias" />"
						class="btn btn-danger"><span
						class="glyphicon glyphicon-plus" aria-hidden="true"></span>
						Agregar</a>
				</p>
              <table class="table" id="tabla">
                    <thead class="thead-inverse table-bordered ">
                        <tr>
                            <th>Nombre</th>
                            <th>Rut Empresa</th>
                            <td>Comuna</td>
                            <td>Ciudad</td>
                            <td>Descripci&oacute;n</td>
                            <th>Opcion</th>
                           
                        </tr>
                    </thead>
                    <tbody class="table-bordered table-hover ">
                    <c:forEach items="${documents}" var="doc">
                    <tr>
                    
						<td>${doc.name}</td>
						<td>${doc.rut}</td>
						<td>${doc.comuna}</td>
						<td>${doc.ciudad}</td>
						<td>${doc.descripcion}</td>
						<td><a href="<c:url value='/edit-vinias-${doc.id}' />" class="btn btn-success custom-width">editar</a>
						<sec:authorize access="hasRole('USER')">
							<a href="<c:url value='/delete-vinia-${doc.id}' />" class="btn btn-danger custom-width">eliminar</a></td>
        				</sec:authorize>
					</tr>
                    </c:forEach>
                    </tbody>
                </table>
                </caption>
			</section> 
			</div>
         
</body>
</html>