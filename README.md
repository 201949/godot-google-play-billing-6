# godot-google-play-billing-6
Godot Android plugin for the Google Play Billing Library version 6 (tested on Godot 3.5.2)

Plugin supports:
- One-time purchases (in-app)
- Repeat purchases (in-app)
- Subscriptions


[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Godot](https://img.shields.io/badge/Godot%20Engine-3.5.2-blue.svg)](https://github.com/godotengine/godot/)
[![GPBL](https://img.shields.io/badge/Google%20Play%20Billing%20Library-6.2.1-green.svg)](https://developer.android.com/google/play/billing/integrate)
[![MIT license](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://github.com/201949/godot-google-play-billing-6/blob/main/LICENSE)

If you want me to continue developing the plugin and keeping it up-to-date, please support me by

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://buymeacoffee.com/magikelle)

Please give a star :star: to the plugin repository if you found it useful.

### Google Play Billing Library version deprecation information
https://developer.android.com/google/play/billing/deprecation-faq

### Generate plugin .aar file
If there is no release for your Godot version, you need to generate new plugin .aar file.  
Follow these instruction: [ official documentation](https://docs.godotengine.org/en/stable/tutorials/plugins/android/android_plugin.html "documentation").

In short follow these steps:

1. Compile the project:

	Open command window and *cd* into *PGSGP* direcory and run command below
	
	* Windows:
	
		gradlew.bat build
		
	* Linux:
	
		./gradlew build
	
2. Copy the newly created .aar and .gdap files to your plugin directory:

*app/build/outputs/aar/GodotGooglePlayBilling-6.X.X-release.aar to *[your godot project]/android/plugins/*
*GodotGooglePlayBilling.gdap to *[your godot project]/android/plugins/*

### Preparing the Editor and Project for Plugin Use

1. First check your Android export template settings. You need to specify a minimum SDK version of 21 and a target SDK version of 34 to meet the Google Play target platform requirements.

![Pic 01](https://raw.githubusercontent.com/201949/godot-google-play-billing-6/main/pic_01.png)

2. Secondly, check the \android\build\config.gradle file and make the necessary changes to the SDK version specification if needed.

![Pic 02](https://raw.githubusercontent.com/201949/godot-google-play-billing-6/main/pic_02.png)

3. Thirdly, in Android export template "Options" section at "Permissions" set "Access Network State" to "On" and set "Internet" to "On".
 Also set some addition permissions at "Custom Permissions": "com.android.vending.BILLING" (might be required for some reason). 

### Example of Usage:
1. Create a singleton instance of the plugin and add it to the AutoLoad section in Project Settings.
2. Call the function pay(name_product) with the desired purchase name to initiate the purchase process.

```
extends Node

const NON_CONSUMABLE_ITEMS:Array = ["purchase1", "purchase2", "purchase3"] # Non-consumable items
const CONSUMABLE_ITEMS:Array = ["pay1", "pay2"] # Consumable items
const SUBSCRIPTION_ITEMS:Array = ["subscription1", "subscription2"] # Subscription items

var payment = null
var test_item_purchase_token = null
var purchasable_inapp:Dictionary = {}
var purchased_inapp:String = ""
var to_buy_item:String = ""

func _ready() -> void:
	if Engine.has_singleton("GodotGooglePlayBilling"):
		payment = Engine.get_singleton("GodotGooglePlayBilling")
		payment.setLogLevel(0)		# Set loglevel 0 - none, 1 - enabled
		payment.setLogTag("godot")	# Set TAG for log
		_connect_signals() # Connect signals to the payment object
		print("Starting connection to Google Play Billing...")
		payment.startConnection() # Start connection to the billing service

func _connect_signals():
	# Connect various signals for handling billing events
	payment.connect("connected", self, "_on_connected")
	payment.connect("disconnected", self, "_on_disconnected")
	payment.connect("connect_error", self, "_on_connect_error")
	payment.connect("purchases_updated", self, "_on_purchases_updated")
	payment.connect("purchase_error", self, "_on_purchase_error")
	payment.connect("product_details_query_completed", self, "_on_product_details_query_completed")
	payment.connect("product_details_query_error", self, "_on_product_details_query_error")
	payment.connect("purchase_acknowledged", self, "_on_purchase_acknowledged")
	payment.connect("purchase_acknowledgement_error", self, "_on_purchase_acknowledgement_error")
	payment.connect("purchase_consumed", self, "_on_purchase_consumed")
	payment.connect("purchase_consumption_error", self, "_on_purchase_consumption_error")
	payment.connect("query_purchases_response", self, "_on_query_purchases_response")

func _on_connected():
	print("CONNECTED!")
	yield(get_tree().create_timer(2), "timeout") # Wait for 2 seconds

	# Request product details for all items
	var all_items = NON_CONSUMABLE_ITEMS + CONSUMABLE_ITEMS
	payment.queryProductDetails(all_items, "inapp")
	payment.queryProductDetails(SUBSCRIPTION_ITEMS, "subs")
	# Query information about purchased items
	payment.queryPurchases("inapp")
	payment.queryPurchases("subs")

func _on_product_details_query_completed(sku_details):
	print("Product details query completed: " + str(sku_details))
	# Store details of each purchasable item
	for available_sku in sku_details:
		purchasable_inapp[available_sku.productId] = available_sku
		var item_price = available_sku["price"]
		print("Price for %s is %s" % [available_sku["productId"], item_price])

		match available_sku["productId"]:
			"purchase1":
				print("Currency Code: ", available_sku["currencyCode"])
				print("Description: ", available_sku["description"])
				print("Formatted Price: ", available_sku["formattedPrice"])
				print("Price: ",available_sku["price"])
				print("Product ID: ",available_sku["productId"])
				print("Title: ", available_sku["title"])
				print("Type: ", available_sku["type"])

func _on_product_details_query_error(code, message, list):
	print("SKU details query error %d: %s" % [code, message])

func _on_query_purchases_response(query_result):
	print("query_purchases_response Check!")
	if query_result and query_result.status == OK:
		if query_result.purchases.empty():
			print("query_result.purchases is empty!")
		else:
			print("query_result.purchases = ", query_result.purchases)
			# Check if there are any non-consumable, consumable purchases, or subscriptions
			for purchase in query_result.purchases:
				if purchase.productId in NON_CONSUMABLE_ITEMS and purchase.purchaseState == 1:
					print("Non-consumable purchase found!")
				elif purchase.productId in CONSUMABLE_ITEMS and purchase.purchaseState == 1:
					print("Consumable purchase found!")
				elif purchase.productId in SUBSCRIPTION_ITEMS and purchase.purchaseState == 1:
					print("Subscription purchase found!")
	else:
		print("Failed to query in-app purchases.")

func _on_purchases_updated(purchases):
	# Handle updated purchases
	for purchase in purchases:
		if not purchase.isAcknowledged:
			if purchase.productId in NON_CONSUMABLE_ITEMS:
				payment.acknowledgePurchase(purchase.purchaseToken) # Non-consumable purchase
			elif purchase.productId in CONSUMABLE_ITEMS:
				payment.consumePurchase(purchase.purchaseToken) # Consumable purchase
	if purchases.size() > 0:
		# Store the last purchase token
		test_item_purchase_token = purchases[purchases.size() - 1].purchaseToken

func _on_purchase_acknowledged(_purchase_token):
	print("Purchase acknowledged: %s" % purchased_inapp)
	# Handle the acknowledged purchase
	_handle_purchase(purchased_inapp)

func _on_purchase_consumed(_purchase_token):
	print("Purchase consumed: %s" % purchased_inapp)
	# Handle the consumed purchase
	_handle_purchase(purchased_inapp)

func _handle_purchase(product_id):
	# Handle specific purchases based on the product ID
	match product_id:
		"purchase1":
			print("Handling purchase1")
		"purchase2":
			print("Handling purchase2")
		"purchase3":
			print("Handling purchase3")
		"pay1":
			$"/root/User".monet += 250
			$"/root/User".save()
			$"/root/Global".pay_true = true
		"pay2":
			$"/root/User".monet += 1500
			$"/root/User".save()
			$"/root/Global".pay_true = true
		"subscription1":
			print("Handling subscription1")
		"subscription2":
			print("Handling subscription2")

func _on_purchase_error(code, message):
	print("Purchase error %d: %s" % [code, message])

func _on_purchase_acknowledgement_error(code, message):
	print("Purchase acknowledgement error %d: %s" % [code, message])

func _on_purchase_consumption_error(code, message, purchase_token):
	print("Purchase consumption error %d: %s, purchase token: %s" % [code, message, purchase_token])

func _on_disconnected():
	print("GodotGooglePlayBilling disconnected. Will try to reconnect in 10s...")
	yield(get_tree().create_timer(10), "timeout") # Wait for 10 seconds
	payment.startConnection() # Attempt to reconnect to the billing service

func pay(name_product):
	# Initiate purchase for the specified product
	if name_product in NON_CONSUMABLE_ITEMS or name_product in CONSUMABLE_ITEMS or name_product in SUBSCRIPTION_ITEMS:
		var type = "inapp"
		if name_product in SUBSCRIPTION_ITEMS:
			type = "subs"
		print("Initiating purchase for product: %s" % name_product)
		print("Initiating purchase with type: %s" % type)
		payment.purchase(name_product, type, "", "")
	else:
		print("Invalid product: %s" % name_product)
```
