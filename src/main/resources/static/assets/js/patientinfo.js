var result_area = document.querySelector("#info-area");
result_area.innerHTML = '<div><p>Please wait...</p></div>';

const patientUrl = "/patient-info" + window.location.search

const data = await d3.json(patientUrl);
if (data.error) {
	var main_area = document.querySelector("#main");
	result_area.innerHTML = '';
	var div = document.createElement('div');
	var head = document.createElement('h3');
	head.innerHTML = 'Error';
	div.appendChild(head);
	var e = document.createElement('div');
	e.appendChild(document.createTextNode(data.error));
	div.appendChild(e);
	result_area.appendChild(div);
}
else {
	console.log(data);
}

result_area.innerHTML = '';
var table = document.createElement('table'),
	tbody = document.createElement('tbody');

for (const [key, value] of Object.entries(data)) {
	var tr = document.createElement('tr'),
		th = document.createElement('th'),
		td = document.createElement('td');
	th.appendChild(document.createTextNode(properDisplay(key)));
	td.appendChild(document.createTextNode(value));
	tr.appendChild(th); tr.appendChild(td);
	tbody.appendChild(tr);
}
table.appendChild(tbody);
result_area.appendChild(table);

function properDisplay(field) {
	switch (field) {
		case "patient_uuid":
			return 'Patient UUID';
		case 'patient_gender':
			return 'Gender';
		case 'patient_birthdate':
			return 'Birthdate';
		case 'patient_age_group':
			return 'Age group';
		case 'patient_group':
			return 'Group';
		case "patient_min_date":
			return "Min date";
		case "patient_max_date":
			return "Max date";
		default:
			return field;
	}
}