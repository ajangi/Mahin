# Screen spec — Pregnancy expenses (M18b)

**Route:** `pregnancy_expenses`  
**States:** loading, empty, populated (list + category summary + entry form)

## Layout

- Header: title + local-only subtitle
- Category summary card when totals exist
- Entry form: category chips, currency chips, amount, optional note, save
- Scrollable expense list with delete

## Copy keys

`pregnancy_expenses_*`, `pregnancy_expense_category_*`

## Privacy

No analytics properties with amounts; excluded from export (see privacy tests).
