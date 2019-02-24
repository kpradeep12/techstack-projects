<#import "template.ftl" as layout />
<@layout.mainLayout>
<a href="/employee?action=new" class="btn btn-secondary float-right mb-1" role="button">New Employee</a>
<table class="table">
    <thead class="thead-dark">
    <tr>
        <th scope="col">Id</th>
        <th scope="col">Name</th>
        <th scope="col">Email</th>
        <th scope="col">City</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <#list employees as emp>
    <tr>
        <td>${emp.id}</td>
        <td>${emp.name}</td>
        <td>${emp.email}</td>
        <td>${emp.city}</td>
        <td>
            <a href="/employee?action=edit&id=${emp.id}" class="btn btn-secondary float-right mr-2" role="button">Edit</a>
            <a href="/delete?id=${emp.id}" class="btn btn-danger float-right mr-2" role="button">Delete</a>
        </td>
    </tr>
    </#list>
    </tbody>
</table>
</@layout.mainLayout>