<!DOCTYPE html>
<head>
    <meta charset="UTF-8">
    <title>Auto Repair Shop</title>
</head>
<body>
    <div>
        <p><b>${product.get().getName()},
                ${product.get().getCountry()},
                ${product.get().getPrice()}</b> </p>
    </div>

    <form action="/prod/{id}" method="post">
        Имя: <label>
            <input type="text" name="name"/>
        </label><br><br>
        producer: <label>
            <input type="text" name="producer"/>
        </label><br><br>
        price: <label>
            <input type="number" name="price"/>
        </label><br><br>
        country: <label>
            <input type="text" name="country"/>
        </label><br><br>
        <input type="submit" value="Добавить"/>
    </form>
    <form action="/prod/{id}" method="delete">
        <input type="submit" value="Удалить">
    </form>

</body>
</html>