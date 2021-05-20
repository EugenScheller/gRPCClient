to build the project use:
mvn clean install
to start the spring boot application use:
mvn spring-boot:start

REST-Port is default: 8090
gRPC-Port is default: 9095

you can find the boilerplate of the project here:
https://github.com/robertdiers/example-spring-boot-rest

commands for endpoints:
create:
curl --request POST \
  --url http://localhost:8090/api/grpccreatesingletutorial \
  --header 'Content-Type: application/json' \
  --data '{"title": "Miller Hills",	"description":"asdf", "published": true}'

createAll:
curl --request POST \
  --url http://localhost:8090/api/grpccreatemultipletutorial \
  --header 'Content-Type: application/json' \
  --data '[
	{
	"title":"Garett O'\''Conner",
	"description":"https://s3.amazonaws.com/uifaces/faces/twitter/rodnylobos/128.jpg",
	"published":true
	},
	{
	"title":"Isaiah Gerlach DVM",
	"description":"https://s3.amazonaws.com/uifaces/faces/twitter/llun/128.jpg",
	"published":true
	},
	{
	"title":"Gina Nicolas",
	"description":"https://s3.amazonaws.com/uifaces/faces/twitter/Elt_n/128.jpg",
	"published":true
	}
]'

getAll:
curl --request GET \
  --url http://localhost:8090/api/grpctutorials \
  --header 'Content-Type: application/json'
  
validate:
curl --request POST \
  --url http://localhost:8090/api/grpcvalidate \
  --header 'Content-Type: application/json' \
  --data '[
	{
	"title":"Mrs. Kolby Davis",
	"description":"https://s3.amazonaws.com/uifaces/faces/twitter/scrapdnb/128.jpg",
	"published":true
	},
	{
	"title":"Garfield Davis",
	"description":"https://s3.amazonaws.com/uifaces/faces/twitter/divya/128.jpg",
	"published":true
	},
	{
	"title":"Ms. Major Toy",
	"description":"https://s3.amazonaws.com/uifaces/faces/twitter/begreative/128.jpg",
	"published":true
	}
]'
