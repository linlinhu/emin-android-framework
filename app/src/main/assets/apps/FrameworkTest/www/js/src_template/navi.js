<script id="navi" type="text/html">
	<ul>
	    <% for (var i = 0; i < list.length; i ++) { %>
	        <li><%= i + 1 %> ：<%= list[i] %></li>
	    <% } %>
	</ul>
</script>