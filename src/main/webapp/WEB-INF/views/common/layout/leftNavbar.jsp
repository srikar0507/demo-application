
<div class="well">
    <div>Menu</div>
    <ul id="nav-tab" class="nav nav-pills nav-stacked">
        <sec:authorize url="/app/admin/h2Console">
            <li><a href="${contextPath}/app/admin/h2Console">H2 Admin Console</a></li>
        </sec:authorize>
        <sec:authorize url="/app/accounts">
            <li><a href="${contextPath}/app/accounts">Accounts Management</a></li>
        </sec:authorize>
    </ul>
</div>
