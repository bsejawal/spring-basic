# spring-basic
```json
{
  "accountNo": "366555",
  "palAccount": "89766",
  "name": "jim adams",
  "positions": [
    {
      "stock": "nvda",
      "value": "1000"
    },
    {
      "stock": "tsla",
      "value": "2000"
    },
    {
      "stock": "goog",
      "value": "3300"
    }
  ],
  "address": "1101 s. park",
  "city": "austin",
  "state": "texas"
}
```

- accept payload as list
- discard duplicate accounts thats in DB before insert
- do bulk insert to two tables with separate threads