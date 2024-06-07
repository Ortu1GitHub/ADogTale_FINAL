<?php

include(__DIR__.'/vendor/autoload.php');

use Stripe\StripeClient;

$stripe = new StripeClient('sk_test_51PO3N2G6nLnZZbCcbHv3g2jaiYtYrb3rV03EF9uwv9G2Zlz7BegvCShDmlC67RdZ1JYYMTeXihQyv377pJsuu96400gxN1ZtRP');

$customer = $stripe->customers->create([
    'name' => 'fakeCustomer',
    'address' => [
        'line1' => 'calle 1',
        'postal_code' => '03300',
        'city' => 'Orihuela',
        'state' => 'Alicante',
        'country' => 'España'
    ]
]);

$ephemeralKey = $stripe->ephemeralKeys->create([
    'customer' => $customer->id,
], [
    'stripe_version' => '2024-04-10',
]);
$paymentIntent = $stripe->paymentIntents->create([
    'amount' =>100,
    'currency' => 'eur',
    'customer' => $customer->id,
    'description' => 'A Dog Tail Subscription for no advertisments',
    'automatic_payment_methods' => [
        'enabled' => 'true',
    ],
]);

$response = json_encode([
    'paymentIntent' => $paymentIntent->client_secret,
    'ephemeralKey' => $ephemeralKey->secret,
    'customer' => $customer->id,
    'publishableKey' => 'pk_test_51PO3N2G6nLnZZbCcFfpExTBvIDNCyJ2uhcaciJh8VXcwVEeLA9GTGbUFalnMGcmIth0WLg1z67NF6nNznIaUo4pV00EbfBR2jE'
]);

header('Content-Type: application/json');

echo($response);

