package com.magikelle.godotgoogleplaybilling.utils;

import org.godotengine.godot.Dictionary;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.ProductDetails;

import java.util.List;

public class GooglePlayBillingUtils {

	// Преобразование списка покупок в массив объектов словарей
	public static Object[] convertPurchaseListToDictionaryObjectArray(List<Purchase> purchaseList) {
		Object[] result = new Object[purchaseList.size()];
		for (int i = 0; i < purchaseList.size(); i++) {
			result[i] = convertPurchaseToDictionary(purchaseList.get(i));
		}
		return result;
	}

	// Преобразование одной покупки в словарь
	public static Dictionary convertPurchaseToDictionary(Purchase purchase) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("orderId", purchase.getOrderId());
		dictionary.put("packageName", purchase.getPackageName());
		dictionary.put("productId", purchase.getProducts().get(0)); // Изменено для использования нового метода getProducts()
		dictionary.put("purchaseTime", purchase.getPurchaseTime());
		dictionary.put("purchaseState", purchase.getPurchaseState());
		dictionary.put("purchaseToken", purchase.getPurchaseToken());
		dictionary.put("signature", purchase.getSignature());
		dictionary.put("isAutoRenewing", purchase.isAutoRenewing());
		dictionary.put("originalJson", purchase.getOriginalJson());
		dictionary.put("isAcknowledged", purchase.isAcknowledged());
		dictionary.put("isAutoRenewing", purchase.isAutoRenewing());
		return dictionary;
	}

	// Преобразование списка деталей продуктов в массив объектов словарей
	public static Object[] convertProductDetailsListToDictionaryObjectArray(List<ProductDetails> productDetailsList) {
		Object[] result = new Object[productDetailsList.size()];
		for (int i = 0; i < productDetailsList.size(); i++) {
			result[i] = convertProductDetailsToDictionary(productDetailsList.get(i));
		}
		return result;
	}

	// Преобразование деталей одного продукта в словарь
	public static Dictionary convertProductDetailsToDictionary(ProductDetails details) {
		Dictionary dictionary = new Dictionary();
		dictionary.put("productId", details.getProductId());
		dictionary.put("title", details.getTitle());
		dictionary.put("description", details.getDescription());
		dictionary.put("type", details.getProductType());

		// Обработка информации о цене в зависимости от типа продукта (однократная покупка или подписка)
		if (details.getOneTimePurchaseOfferDetails() != null) {
			dictionary.put("price", details.getOneTimePurchaseOfferDetails().getPriceAmountMicros() / 1000000.0);
			dictionary.put("currencyCode", details.getOneTimePurchaseOfferDetails().getPriceCurrencyCode());
		} else if (details.getSubscriptionOfferDetails() != null && !details.getSubscriptionOfferDetails().isEmpty()) {
			// Использование первой подписки в качестве примера
			ProductDetails.SubscriptionOfferDetails subscriptionOfferDetails = details.getSubscriptionOfferDetails().get(0);
			dictionary.put("price", subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getPriceAmountMicros() / 1000000.0);
			dictionary.put("currencyCode", subscriptionOfferDetails.getPricingPhases().getPricingPhaseList().get(0).getPriceCurrencyCode());
		}

		return dictionary;
	}
}