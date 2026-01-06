# Quicky Shoppy - Functional Specification

## Overview

**Quicky Shoppy** is an AI-powered shopping list application for iOS (iPhone) that helps users create, organize, and manage their shopping lists with intelligent features. The app uses advanced AI technology to automatically categorize items and extract ingredients from recipe photos.

**Key Value Proposition:** Transform the traditional shopping list experience by combining manual item entry with AI-powered features like automatic categorization and recipe photo scanning.

---

## AI Models and APIs

### Anthropic Claude API

**Model:** Claude Sonnet 4.5 (`claude-sonnet-4-5-20250929`)

**API Endpoint:** `https://api.anthropic.com/v1/messages`

**Authentication:** Requires user to provide their own Claude API key

**Two Primary Use Cases:**

1. **Automatic Item Categorization (Text API)**
   - Analyzes item names to determine appropriate category
   - Runs automatically when items are added
   - Max tokens: 50
   - Response time: ~1-2 seconds

2. **Recipe Photo Ingredient Extraction (Vision API)**
   - Analyzes recipe photos to identify ingredients
   - Extracts ingredient names and quantities
   - Handles handwritten and printed recipes
   - Max tokens: 1024
   - Response time: ~3-5 seconds

---

## Core Features

### 1. Shopping List Management

**Basic List Operations:**
- Users maintain a single, unified shopping list
- Add items manually by typing
- Mark items as complete or incomplete
- Delete individual items
- Delete all items at once
- Delete only completed items
- Items are automatically saved and persist between app sessions

**Item Organization:**
- Items are automatically grouped by category
- Each category has a distinctive emoji icon
- Items can be manually moved between categories
- Completed items appear with strikethrough and checkmark

### 2. AI-Powered Categorization

**Automatic Categorization:**
- When an item is added, AI analyzes it and assigns a category
- Happens automatically in the background
- Item initially appears as "Uncategorised" then moves to correct category
- No user action required

**17 Available Categories:**
1. **Uncategorised** (❓) - Default before AI categorization
2. **Vegetables and Fruits** (🥬) - Fresh produce
3. **Vegetarian** (🥗) - Plant-based items
4. **Glucose-Free** (🚫) - Sugar-free products
5. **Lactose-Free** (🥥) - Dairy-free alternatives
6. **Bread Products** (🍞) - Baked goods, grains
7. **Sweets** (🍬) - Candy, desserts
8. **Pantry** (🏺) - Dry goods, spices, oils
9. **Milk Products** (🥛) - Dairy items
10. **Meat and Seafood** (🥩) - Proteins
11. **Eggs** (🥚) - Egg products
12. **Coffee & Tea** (☕) - Hot beverages
13. **Household Supplies** (🧹) - Cleaning, paper products
14. **Beverages** (🥤) - Soft drinks, juices
15. **Refrigerated Items** (🧊) - Items requiring refrigeration
16. **Electronics** (📱) - Tech items, batteries
17. **Other** (🔷) - Miscellaneous items

**Manual Override:**
- Users can manually move any item to a different category
- Useful when AI categorization doesn't match user preference

### 3. Recipe Photo Processing

**Photo Capture:**
- Take photos directly with camera
- Select existing photos from photo library
- Supports both handwritten and printed recipes

**AI Analysis:**
- App displays "Analyzing recipe..." while processing
- AI extracts all ingredients with quantities
- Results appear in an ingredient selection screen

**Unit Conversion:**
- Automatically converts imperial to metric units
- Conversions include:
  - Cups → milliliters (ml)
  - Ounces → grams (g)
  - Tablespoons → milliliters (ml)
  - Teaspoons → milliliters (ml)
  - Pounds → grams (g)

**Selective Import:**
- All ingredients are pre-selected by default
- Users can deselect unwanted ingredients
- Tap "Add Selected" to import chosen ingredients
- Each ingredient is added as a separate item with quantity
- AI then categorizes each ingredient automatically

### 4. Item Links (Recipe URLs)

**Linking Capability:**
- Attach URLs to any shopping item (e.g., recipe websites)
- Visual indicator shows which items have links
- Quick access to recipe source while shopping

**Link Management:**
- Add link to item
- Edit existing link
- Remove link
- Open link in browser with double-tap

### 5. List Export and Sharing

**Copy to Clipboard:**
- Export entire shopping list as formatted text
- Format includes:
  - Categories as headers
  - Items as bullet points
  - Quantities shown
  - Checkmarks for completed items

**Sharing:**
- Paste exported list into any app
- Designed for messaging apps (WhatsApp, iMessage, etc.)
- Maintains readable formatting

### 6. Cross-App Integration

**Import from Other Apps:**
- Other apps can send items to Quicky Shoppy
- Uses URL scheme: `quickyshoppy://import`
- Supports two import methods:
  1. Direct URL scheme with data
  2. Clipboard-based import

**Import Process:**
- Importing app sends data
- Quicky Shoppy opens automatically
- Import confirmation screen shows all items grouped by category
- Items display with quantities and links (if provided)
- User can select/deselect items before importing
- Tap "Add Selected" to complete import

**Import Data Format:**
- Each item can include:
  - Name (required)
  - Quantity (optional)
  - Category (optional)
  - Recipe link (optional)

---

## User Screens

### Main Shopping List Screen

**Purpose:** Primary screen for viewing and managing the shopping list

**Elements:**
- Text input field at top for adding new items
- Camera button for recipe photo import
- Add button to manually add typed items
- Grouped list showing all items organized by category
- Empty state with shopping cart icon when list is empty
- Toolbar with settings, copy, and delete buttons

**Interactions:**
- Type item name and press Return or tap + to add
- Tap camera button to scan recipe
- Swipe right on item to mark complete/incomplete
- Swipe left on item to delete
- Long-press item for options menu
- Double-tap item with link to open URL

### Settings Screen

**Purpose:** Configure Claude API key

**Elements:**
- API key input field (secure, can show/hide)
- Eye icon to toggle visibility
- API key status indicator (shows if key is configured)
- Save button to store API key
- Clear button to remove API key
- Link to Anthropic console to obtain API key

**Note:** Users must obtain their own Claude API key from Anthropic

### Ingredient Selection Screen

**Purpose:** Review and select ingredients extracted from recipe photo

**Elements:**
- List of detected ingredients
- Each ingredient shows:
  - Name
  - Quantity (if detected)
  - Checkbox for selection
- "Select All" / "Deselect All" button
- "Add Selected" button to import
- Cancel button to abort

**Behavior:**
- All ingredients pre-selected by default
- Users tap checkboxes to deselect unwanted items
- Only selected items are added to shopping list

### Import Confirmation Screen

**Purpose:** Review items being imported from other apps

**Elements:**
- Items grouped by category (if category provided)
- Each item shows:
  - Name
  - Quantity (if provided)
  - Link icon (if URL provided)
  - Checkbox for selection
- "Select All" / "Deselect All" button
- "Add Selected" button to import
- Cancel button to abort

**Behavior:**
- All items pre-selected by default
- Users can review metadata (quantity, links) before importing
- Only selected items are added to shopping list

---

## User Workflows

### Workflow 1: Adding Items Manually

1. User types item name in text field
2. User presses Return key or taps + button
3. Item appears immediately under "Uncategorised" category
4. Within 1-2 seconds, AI categorizes item
5. Item automatically moves to appropriate category
6. Item remains visible throughout categorization

**Example:**
- User types "bananas"
- Item appears under "Uncategorised"
- AI categorizes it
- Item moves to "Vegetables and Fruits" category

### Workflow 2: Scanning Recipe Photos

1. User taps camera button in toolbar
2. System prompts to choose camera or photo library
3. User takes photo or selects existing photo of recipe
4. App displays "Analyzing recipe..." overlay
5. AI processes photo (3-5 seconds)
6. Ingredient selection screen appears with detected ingredients
7. All ingredients are pre-selected
8. User deselects any unwanted ingredients
9. User taps "Add Selected"
10. Selected ingredients appear in shopping list with quantities
11. AI categorizes each ingredient in background
12. Ingredients move to appropriate categories

**Example:**
- User photographs chocolate cake recipe
- AI detects: "2 cups flour", "1 cup sugar", "3 eggs", "1 cup milk"
- User deselects "milk" (already have it)
- Adds remaining ingredients
- Items appear as:
  - "flour (473ml)" → moves to Bread Products
  - "sugar (237ml)" → moves to Sweets
  - "eggs (3)" → moves to Eggs

### Workflow 3: Managing Items

**Completing an Item:**
1. User swipes right on item
2. Item gets checkmark and strikethrough
3. Item remains visible in list

**Uncompleting an Item:**
1. User swipes right on completed item
2. Checkmark and strikethrough removed
3. Item appears as active again

**Deleting an Item:**
1. User swipes left on item
2. Item is immediately removed from list

**Moving Item to Different Category:**
1. User long-presses on item
2. Context menu appears
3. User selects "Move to..."
4. Category selection menu appears
5. User taps desired category
6. Item moves to new category

**Adding a Link to Item:**
1. User long-presses on item
2. Context menu appears
3. User selects "Add Link"
4. URL input dialog appears
5. User enters URL
6. Link is saved, item shows link indicator

**Opening Item Link:**
1. User double-taps on item with link
2. URL opens in Safari browser

### Workflow 4: Exporting Shopping List

1. User taps clipboard icon in toolbar
2. Entire list is copied to clipboard with formatting
3. Confirmation alert appears
4. User switches to messaging app
5. User pastes list
6. List appears formatted with categories, items, and checkmarks

**Example Output:**
```
🥬 Vegetables and Fruits
• bananas
✓ apples (completed)

🍞 Bread Products
• whole wheat bread

🥛 Milk Products
• milk (1L)
```

### Workflow 5: Bulk Deletion

**Delete Completed Items:**
1. User taps trash icon in toolbar
2. Menu appears with options
3. User selects "Delete Completed Items"
4. Confirmation dialog appears
5. User confirms
6. All completed items are removed

**Delete All Items:**
1. User taps trash icon in toolbar
2. Menu appears with options
3. User selects "Delete All Items"
4. Confirmation dialog appears
5. User confirms
6. Entire list is cleared

### Workflow 6: Importing from Another App

**URL Scheme Import:**
1. User is in another app that supports Quicky Shoppy
2. User triggers "Send to Quicky Shoppy" action
3. Other app constructs URL with item data
4. Quicky Shoppy opens automatically
5. Import confirmation screen appears
6. Items are grouped by category
7. All items are pre-selected
8. User reviews items, deselects unwanted ones
9. User taps "Add Selected"
10. Selected items appear in shopping list
11. AI categorizes any items without categories

**Clipboard Import (Alternative):**
1. Other app copies import data to clipboard
2. User switches to Quicky Shoppy
3. App detects import data on clipboard
4. Import confirmation screen appears
5. Same selection process as URL scheme import

### Workflow 7: First-Time Setup

1. User launches app for first time
2. Empty shopping list appears
3. User taps settings icon
4. User visits Anthropic website to obtain API key
5. User copies API key
6. User pastes API key into settings
7. User taps "Save API Key"
8. Confirmation appears
9. User returns to main screen
10. AI features are now active

---

## Key Behaviors and Rules

### Data Persistence
- All items are automatically saved
- List persists when app is closed
- API key is securely stored
- No cloud sync (local storage only)

### AI Categorization
- Happens automatically for all new items
- Takes 1-2 seconds typically
- Item visible throughout process
- User can manually override AI choice
- Recategorization doesn't occur automatically after manual override

### Recipe Photo Processing
- Supports JPEG images only
- Maximum image size processed: standard phone camera resolution
- Both color and black/white photos supported
- Handwriting recognition included
- Quantity extraction is best-effort (may not always detect)

### Item Quantities
- Displayed in parentheses after item name
- Optional - items can exist without quantities
- From recipe photos: automatically converted to metric
- From manual entry: user can include quantity in item name
- No automatic parsing of manually entered quantities

### Completion Status
- Completed items remain in list until explicitly deleted
- Completed items can be uncompleted
- Completion status preserved when moving categories
- Bulk delete option removes only completed items

### Cross-App Integration
- Import is non-destructive (adds to existing list)
- Duplicate checking is not performed
- Category assignment from importing app is respected
- User can reject entire import by canceling

### Error Handling
- Invalid API key: Settings show error message
- Recipe photo analysis fails: User sees error alert
- Network errors: User sees appropriate error message
- Import data format errors: Import is rejected with error

---

## Platform and Requirements

**Platform:** iOS (iPhone)

**Framework:** Native iOS app

**Network Requirements:**
- Internet connection required for AI features
- Works offline for basic list management
- Offline items can be categorized when connection restored

**User Requirements:**
- Claude API key from Anthropic (user must obtain separately)
- API key requires Anthropic account
- API usage incurs costs based on Anthropic's pricing

**Privacy:**
- All data stored locally on device
- Shopping list data sent to Claude API for categorization
- Recipe photos sent to Claude API for analysis
- No data stored by app developer
- All data transmission to Anthropic only

---

## Integration Specifications

### URL Scheme

**Scheme:** `quickyshoppy://`

**Import Endpoint:** `quickyshoppy://import?data=<base64_encoded_json>`

**Data Format (before base64 encoding):**
```json
{
  "items": [
    {
      "name": "Item name",
      "quantity": "Amount (optional)",
      "category": "Category name (optional)",
      "recipeLink": "https://recipe-url.com (optional)"
    }
  ]
}
```

**Valid Category Names:**
- "Uncategorised"
- "Vegetables and Fruits"
- "Vegetarian"
- "Glucose-Free"
- "Lactose-Free"
- "Bread Products"
- "Sweets"
- "Pantry"
- "Milk Products"
- "Meat and Seafood"
- "Eggs"
- "Coffee & Tea"
- "Household Supplies"
- "Beverages"
- "Refrigerated Items"
- "Electronics"
- "Other"

**Integration Example:**
Third-party app creates JSON, base64-encodes it, constructs URL, and opens URL to trigger import.

### Clipboard Import

**Alternative Method:** Apps can copy the same JSON to clipboard (not base64-encoded) and user manually switches to Quicky Shoppy.

---

## Future Considerations (Not Currently Implemented)

*These items are not part of the current functional specification but could be considered for future versions:*

- Multiple shopping lists
- Cloud synchronization
- Shared lists between users
- Barcode scanning
- Price tracking
- Store locations
- List history
- Item suggestions based on history
- Dark mode
- iPad support
- Widget support
- Siri integration
- Push notifications
- Recipe storage within app

---

## Summary

Quicky Shoppy is a modern shopping list app that enhances the traditional list-making experience with AI capabilities. Users can quickly add items manually, scan recipes to extract ingredients automatically, and benefit from intelligent categorization—all while maintaining full control over their list organization and sharing.

The app's integration capabilities allow it to work seamlessly with other apps in the user's ecosystem, and its AI-powered features reduce manual work while keeping the user in control of the final list composition.

**Last Updated:** January 2026
