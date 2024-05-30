# godot-google-play-billing-6
Godot 3.5.2 plugin for Google Play Billing Library 6 on Android

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Godot](https://img.shields.io/badge/Godot%20Engine-3.5.2-blue.svg)](https://github.com/godotengine/godot/)
[![GPBL](https://img.shields.io/badge/Google%20Play%20Billing%20Library-6.2.1-green.svg)](https://developer.android.com/google/play/billing/integrate)
[![MIT license](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://lbesson.mit-license.org/)

### Google Play Billing Library version deprecation information
https://developer.android.com/google/play/billing/deprecation-faq

### Generate plugin .aar file
If there is no release for your Godot version, you need to generate new plugin .aar file.  
Follow these instruction: [ official documentation](https://docs.godotengine.org/en/stable/tutorials/plugins/android/android_plugin.html "documentation").

In short follow these steps:

1. Download [ AAR library for Android plugins](https://godotengine.org/download/windows "Godot download").

2. Copy .aar file to *godot-lib.release/* and rename it to *godot-lib.release.aar*

3. Compile the project:

	Open command window and *cd* into *PGSGP* direcory and run command below
	
	* Windows:
	
		gradlew.bat build
		
	* Linux:
	
		./gradlew build
	
4. Copy the newly created .aar and .gdap files to your plugin directory:

*app/build/outputs/aar/GodotGooglePlayBilling-6.X.X-release.aar to *[your godot project]/android/plugins/*
*GodotGooglePlayBilling.gdap to *[your godot project]/android/plugins/*

### Example of Usage:
1. Create a singleton instance of the plugin and add it to the AutoLoad section in Project Settings.
2. Call the function pay(name_product) with the desired purchase name to initiate the purchase process.

```
extends Node

const TEST_ITEM_SKU:Array = ["purchase1","purchase2","purchase3"] # существующие товары в игре

var payment = null
var test_item_purchase_token = null
var purchasable_inapp:Dictionary = {}

func _ready() -> void:
	if Engine.has_singleton("GodotGooglePlayBilling"):
		payment = Engine.get_singleton("GodotGooglePlayBilling")
		# No params.
		payment.connect("connected", self, "_on_connected")
		# No params.
		payment.connect("disconnected", self, "_on_disconnected")
		# Response ID (int), Debug message (string).
		payment.connect("connect_error", self, "_on_connect_error")
		# Purchases (Dictionary[]).
		payment.connect("purchases_updated", self, "_on_purchases_updated")
		# Response ID (int), Debug message (string).
		payment.connect("purchase_error", self, "_on_purchase_error")
		# SKUs (Dictionary[]).
		payment.connect("product_details_query_completed", self, "_on_product_details_query_completed")
		# Response ID (int), Debug message (string), Queried SKUs (string[]).
		payment.connect("product_details_query_error", self, "_on_product_details_query_error")
		# Purchase token (string).
		payment.connect("purchase_acknowledged", self, "_on_purchase_acknowledged")
		# Response ID (int), Debug message (string), Purchase token (string).
		payment.connect("purchase_acknowledgement_error", self, "_on_purchase_acknowledgement_error")
		# Purchase token (string).
		payment.connect("purchase_consumed", self, "_on_purchase_consumed")
		# Response ID (int), Debug message (string), Purchase token (string).
		payment.connect("purchase_consumption_error", self, "_on_purchase_consumption_error")
		# Response for query purchases.
		payment.connect("query_purchases_response", self, "_on_query_purchases_response")

		payment.startConnection()

func _on_connected():
	print("CONNECTED!")
	yield(get_tree().create_timer(2), "timeout")

	# Создаёт запрос на получение информации о товарах
	payment.queryProductDetails(TEST_ITEM_SKU, "inapp")

	# Запрос информации о купленных товарах.
	payment.queryPurchases("inapp")

func _on_product_details_query_completed(sku_details):
	print("Product details query completed: " + str(sku_details))
	for available_sku in sku_details:
		purchasable_inapp[available_sku.productId] = available_sku
		match available_sku["productId"]:
			"purchase1":
				var ads_remove_price = available_sku["price"]
      "purchase2":
				var ads_remove_price = available_sku["price"]
      "purchase3":
				var ads_remove_price = available_sku["price"]

func _on_product_details_query_error(code, message):
	print("SKU details query error %d: %s" % [code, message])

func _on_query_purchases_response(query_result):
	print("query_purchases_response Check!")
	if query_result and query_result.status == OK:
		if query_result.purchases.empty():
			print("query_result.purchases is empty!")
		else:
			print("query_result.purchases = ", query_result.purchases)
			for purchase in query_result.purchases:
				if (purchase.productId == "purchase1" or purchase.productId == "purchase2" or purchase.productId == "purchase3") and purchase.purchaseState == 1:
					print("Purchases Found!")
	else:
		print("Failed to query in-app purchases.")

func _on_purchases_updated(purchases):
	purchased_inapp = to_buy_item
	for purchase in purchases:
		if not purchase.is_acknowledged:
			# payment.consumePurchase(purchase.purchase_token) # многоразовая покупка
			payment.acknowledgePurchase(purchase.purchase_token) # одноразовая покупка
	if purchases.size() > 0:
		test_item_purchase_token = purchases[purchases.size() - 1].purchase_token

var purchased_inapp:String
# одноразовые покупки
func _on_purchase_acknowledged(_purchase_token):
	match purchased_inapp:
		"purchase1":
			print("Purchased purchase1")
		"purchase2":
			print("Purchased purchase2")
		"purchase3":
			print("Purchased purchase3")

# многоразовые покупки
func _on_purchase_consumed(_purchase_token):
	match purchased_inapp:
		"pay1":
			$"/root/User".monet += 200
			$"/root/User".save()
			$"/root/Global".pay_true = true
		"pay2":
			$"/root/User".monet += 1205
			$"/root/User".save()
			$"/root/Global".pay_true = true

func _on_purchase_error(code, message):
	print("Purchase error %d: %s" % [code, message])

func _on_purchase_acknowledgement_error(code, message):
	print("Purchase acknowledgement error %d: %s" % [code, message])

func _on_purchase_consumption_error(code, message, purchase_token):
	print("Purchase consumption error %d: %s, purchase token: %s" % [code, message, purchase_token])

func _on_disconnected():
	print("GodotGooglePlayBilling disconnected. Will try to reconnect in 10s...")
	yield(get_tree().create_timer(10), "timeout")
	payment.startConnection()

var to_buy_item:String

func pay(name_product):
	payment.purchase(name_product, "inapp", "", "") # Добавлены параметры
	to_buy_item = name_product
```
