# App name opensourcenamegeneration

## There are some examples of json request body to send:

* ### count 2, frequency rare, type surname, 
{
  "count": 2,
  "frequency": "rare",
   "type": "surname"
}

* ### count 3, frequency common, type female, include surname
{
  "count": 3,
  "frequency": "common",
   "type": "female",
   "withSurname": true
}

* ### count 1, frequency all, type any, include surname
{
  "count": 1,
  "frequency": "all",
   "type": "any",
   "withSurname": true
}