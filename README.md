# Godot Google Play Billing 7

A Godot plugin for integrating Google Play Billing Library version 7.1.0 with Godot 3.5.2
(Can also be used for newer 3.X-4.X versions, but requires a little bit more work. See bellow for more info.)

[![Android](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)
[![Godot](https://img.shields.io/badge/Godot%20Engine-3.5.2-blue.svg)](https://github.com/godotengine/godot/)
[![GPBL](https://img.shields.io/badge/Google%20Play%20Billing%20Library-7.1.0-green.svg)](https://developer.android.com/google/play/billing/integrate)
[![MIT license](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://github.com/201949/godot-google-play-billing-7/blob/main/LICENSE)

## Supported Features

- **One-time in-app purchases**
- **Repeat in-app purchases**
- **Subscriptions**

## Disclaimer

If you want me to continue developing the plugin and keeping it up-to-date, please support me by:

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://buymeacoffee.com/magikelle)

Please also consider giving a star :star: to the plugin repository if you found it useful.

## Supporters

A big thank you to the following people for their sponsorship:
- [davekaa](https://github.com/davekaa)
- [mackatap](https://github.com/mackatap)
- [galaxiusgames](https://github.com/galaxiusgames)

## Production use
*Google Play Store:*

- ["Ugly Button Adventure"](https://play.google.com/store/apps/details?id=com.magikelle.uglybutton) by [Magikelle Studio aka Ugly Button](https://play.google.com/store/apps/dev?id=8681639065134696403)
- ["Ugly Button 2"](https://play.google.com/store/apps/details?id=com.magikelle.uglybutton.chapter.two) by [Magikelle Studio aka Ugly Button](https://play.google.com/store/apps/dev?id=8681639065134696403)
- ["Stunt Riders"](https://play.google.com/store/apps/details?id=com.magikelle.bikeriders) by [Magikelle Studio aka Ugly Button](https://play.google.com/store/apps/dev?id=8681639065134696403)
- ["Lifty Circus Action Platformer"](https://play.google.com/store/apps/details?id=com.magikelle.liftycircus) by [Magikelle Studio aka Ugly Button](https://play.google.com/store/apps/dev?id=8681639065134696403)


## Google Play Billing Library Version Deprecation Information

For information on version deprecation, visit: [Google Play Billing Library Deprecation FAQ](https://developer.android.com/google/play/billing/deprecation-faq)

## Compiling the plugin .aar file for any versions of Godot libraries

If there is no release for your Godot version, you will need to generate a new plugin .aar file.  

1. Go to the downloads page of your desired Godot version
2. Click "Show all downloads"
3. Download the .AAR Library
4. Clone this repository
5. Place the newly downloaded .AAR file into the root of the cloned repository
6. In the 'app' folder, change the following line on the `build.gradle` file:

   from this: `compileOnly 'org.godotengine:godot:3.5.2.stable'`

   to this: `compileOnly fileTree(dir: '..', include: ['godot-lib*.aar'])`
7. Open a command window (or terminal) and *cd* into the `godot-google-play-billing-7` directory, then run the appropriate command:

    * Windows:
    
        ```bash
        gradlew.bat build
        ```
        
    * Linux:
    
        ```bash
        ./gradlew build
        ```
    *The newly generated .AAR plugin file will be located in `app/build/outputs/aar`*

8. Take the 'release' .AAR file from the directory above, along with the `GodotGooglePlayBilling.gdap` file from the root directory, and place them both in your Godot project under `android/plugins`
   
    Copy the newly created `.aar` and `.gdap` files to your plugin directory:

    from `app/build/outputs/aar/GodotGooglePlayBilling-7.X.X-release.aar` to `[your godot project]/android/plugins/`
   
    and
   
    from `GodotGooglePlayBilling.gdap` to `[your godot project]/android/plugins/`

After that, you will have a plugin for the Godot version you need.

Don't forget to enable the plugin in Godot under your export settings!

**Alternatively, you can download the precompiled plugin files for Godot 3.5.2 from the [releases page](https://github.com/201949/godot-google-play-billing-7/releases/tag/7.1.0_init).**

## Preparing the Editor and Project for Plugin Use

1. Check your Android export template settings. You need to specify a minimum SDK version of 21 and a target SDK version of 35 to meet the Google Play target platform requirements.

    ![Pic 01](https://raw.githubusercontent.com/201949/godot-google-play-billing-7/main/pic_01.png)

2. Check the `android/build/config.gradle` file and make any necessary changes to the SDK version specification.

    ![Pic 02](https://raw.githubusercontent.com/201949/godot-google-play-billing-7/main/pic_02.png)

3. In the Android export template "Options" section under "Permissions", set "Access Network State" and "Internet" to "On". Also, add the following permission under "Custom Permissions": `com.android.vending.BILLING` (this may be required).

## Example of Usage on Godot 3.5.X-3.6:

1. Create a singleton instance of the plugin and add it to the AutoLoad section in Project Settings.
2. Call the function `pay(name_product)` with the desired purchase name to initiate the purchase process.

```gdscript
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
        payment.setLogLevel(0)        # Set log level: 0 - none, 1 - enabled
        payment.setLogTag("godot")    # Set TAG for log
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
    print("Requested products: ", str(list))

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
    # Additional query information about purchased items after acknowledgement
    payment.queryPurchases("inapp")
    payment.queryPurchases("subs")

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

## Upcoming Improvements

Stay tuned for updates, and feel free to [open an issue](https://github.com/201949/godot-google-play-billing-7/issues) or [contribute](https://github.com/201949/godot-google-play-billing-7/pulls) if you have any suggestions or feedback!
