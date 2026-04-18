INSERT INTO product_price_type (id, name) VALUES
    (1, 'common'),
    (2, 'offer'),
    (3, 'black_friday');


INSERT INTO product_category (id, name, description) VALUES
    (1, 'Home Appliances', 'Refrigerators, stoves, washing machines, and small domestic appliances.'),
    (2, 'Electronics, Audio & Video', 'Televisions, headphones, cameras, and audio accessories.'),
    (3, 'Computing', 'Laptops, monitors, PC components, and peripherals.'),
    (4, 'Cell Phones & Phones', 'Smartphones, smartwatches, and telephony accessories.'),
    (5, 'Accessories for Vehicles', 'Automotive parts, tires, tools, and accessories for cars and motorcycles.'),
    (6, 'Beauty & Personal Care', 'Makeup, perfumes, hair care, and skincare products.'),
    (7, 'Home, Furniture & Garden', 'Furniture for living rooms, bedrooms, kitchens, lighting, and decor items.'),
    (8, 'Sports & Fitness', 'Sportswear, gym equipment, supplements, and camping gear.'),
    (9, 'Toys & Hobbies', 'Board games, action figures, collectibles, and model building.'),
    (10, 'Fashion', 'Clothing, footwear, bags, and fashion accessories for men and women.'),
    (11, 'Tools', 'Power tools, hand tools, and construction accessories.'),
    (12, 'Health & Medical', 'Medical equipment, orthopedics, and hygiene products.'),
    (13, 'Video Games', 'Consoles, games, and accessories for PC and video game systems.'),
    (14, 'Food & Beverages', 'Grocery items, alcoholic and non-alcoholic beverages.'),
    (15, 'Babies', 'Baby clothing, diapers, strollers, and maternity accessories.'),
    (16, 'Books, Magazines & Comics', 'National and foreign literature, graphic novels, and special editions.'),
    (17, 'Musical Instruments', 'Guitars, keyboards, drums, and studio equipment.'),
    (18, 'Pet Supplies', 'Pet food, toys, and accessories for dogs, cats, and other pets.'),
    (19, 'Agro', 'Tractors, seeds, tools, and supplies for agriculture and livestock.'),
    (20, 'Industry & Office', 'Industrial equipment, packaging, and commercial furniture.');

INSERT INTO stock_movement_type (id, name) VALUES
    (1, 'ENTRY'),
    (2, 'SALE'),
    (3, 'REFOUND'),
    (4, 'ADJUST'),
    (5, 'OTHER');
