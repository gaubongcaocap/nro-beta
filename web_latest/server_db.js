/*
 * server_db.js
 *
 * This server provides a simple JSON API for administering shops, players
 * and gift codes directly against a MySQL database. It serves a static
 * front‑end from the ./web directory and exposes endpoints to list and
 * update shop items, list players and their items, and create gift codes.
 *
 * The server uses mysql2/promise to connect to the database. Connection
 * parameters are read from db_config.json located alongside this file.
 * You must run `npm install mysql2` in your project before starting
 * the server. See db_config.json for configuration format.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const mysql = require('mysql2/promise');

// Location of static files and DB config
const WEB_DIR = path.join(__dirname, 'web');
const DB_CONFIG_FILE = path.join(__dirname, 'db_config.json');

// Caches
let shopsData = [];
let playersData = [];
let itemTemplates = [];
let itemOptionTemplates = [];
const npcTemplateMap = {};
const itemTemplateMap = {};
// Map option ID to its description. Filled in loadItemOptionTemplates().
const optionTemplateMap = {};

let dbPool;

/**
 * Initialise a pooled MySQL connection using parameters from db_config.json
 */
async function initDbPool() {
  const raw = fs.readFileSync(DB_CONFIG_FILE, 'utf8');
  const config = JSON.parse(raw);
  dbPool = await mysql.createPool(config);
}

/**
 * Load all NPC templates into memory. Each entry contains a name and an
 * optional iconID (pulled from the `head` field) which is used to build
 * an icon path for the front‑end. If the iconID is missing, a
 * placeholder image will be used client side.
 */
async function loadNpcTemplates() {
  // In your database the NPC avatar column is named `avatar` and contains the icon ID.
  const [rows] = await dbPool.query('SELECT id, name, avatar FROM npc_template');
  rows.forEach(r => {
    npcTemplateMap[r.id] = {
      name: r.name,
      iconID: r.avatar !== null ? parseInt(r.avatar, 10) : -1
    };
  });
}

/**
 * Load all item templates from the database. Each entry contains an id,
 * name and an iconID which is used to build the icon path. These
 * templates are used both for rendering items in shops and for giftcode
 * creation.
 */
async function loadItemTemplates() {
  // In your database the item name column is `NAME` and the icon column is `icon_id`.
  const [rows] = await dbPool.query('SELECT id, NAME AS name, icon_id FROM item_template');
  itemTemplates = rows.map(r => ({ id: r.id, name: r.name }));
  rows.forEach(r => {
    itemTemplateMap[r.id] = {
      name: r.name,
      iconID: r.icon_id !== null ? parseInt(r.icon_id, 10) : -1
    };
  });
}

/**
 * Load option templates from the database. Each entry contains an id
 * and a description. Descriptions are used on the front‑end when
 * presenting option selections.
 */
async function loadItemOptionTemplates() {
  // Use correct column names: NAME for description
  const [rows] = await dbPool.query('SELECT id, NAME AS name FROM item_option_template');
  itemOptionTemplates = rows.map(r => ({ id: r.id, description: r.name }));
  // Fill mapping for option descriptions
  rows.forEach(r => {
    optionTemplateMap[r.id] = r.name;
  });
}

/**
 * Parse a raw options structure from the database into an array of
 * objects with numeric id and param. The `options` field in the DB is
 * stored either as a JSON array of objects or as a semicolon separated
 * string (e.g. "47:5;33:200"). Returns an empty array if no options.
 */
function parseOptions(raw) {
  if (!raw) return [];
  try {
    const arr = typeof raw === 'string' ? JSON.parse(raw) : raw;
    if (Array.isArray(arr)) {
      return arr.map(o => ({ id: parseInt(o.id, 10), param: parseInt(o.param, 10) }));
    }
  } catch (e) {
    // fall through
  }
  // Try splitting string
  if (typeof raw === 'string') {
    return raw.split(';').map(pair => {
      const [idStr, paramStr] = pair.split(':');
      const id = parseInt(idStr, 10);
      const param = parseInt(paramStr, 10);
      return { id, param };
    }).filter(o => !isNaN(o.id) && !isNaN(o.param));
  }
  return [];
}

/**
 * Load all shops and their tabs from the database. For each tab we parse
 * the `items` JSON array into a friendly structure for the front‑end. The
 * raw representation (compatible with the game's DB schema) is also kept
 * alongside so that we can write back updates easily.
 */
async function loadShops() {
  const sql = `SELECT s.id AS shop_id, s.npc_id, t.tab_index, t.tab_name, t.items AS items_json
               FROM shop s JOIN tab_shop t ON s.id = t.shop_id
               ORDER BY s.id, t.tab_index`;
  const [rows] = await dbPool.query(sql);
  const map = new Map();
  for (const r of rows) {
    if (!map.has(r.shop_id)) {
      const npc = npcTemplateMap[r.npc_id] || { name: `NPC ${r.npc_id}`, iconID: -1 };
      map.set(r.shop_id, {
        shopId: r.shop_id,
        npcId: r.npc_id,
        npcName: npc.name,
        npcIcon: npc.iconID >= 0 ? `/icons/x4/${npc.iconID}.png` : 'placeholder_npc.png',
        tabs: []
      });
    }
    const shop = map.get(r.shop_id);
    // Parse raw items from JSON in DB
    let rawItems = [];
    if (r.items_json) {
      try { rawItems = JSON.parse(r.items_json); } catch (_) { rawItems = []; }
    }
    // Convert raw items to front‑end friendly items
    const parsedItems = rawItems.filter(it => it && it.is_sell !== false).map(it => {
      const tempId = parseInt(it.temp_id, 10);
      const tpl = itemTemplateMap[tempId] || { name: `Item ${tempId}`, iconID: -1 };
      // Map type_sell numeric to currency string
      const currency = it.type_sell === 1 ? 'ngọc'
        : it.type_sell === 3 ? 'ruby'
        : it.type_sell === 4 ? 'coupon'
        : 'vàng';
      const icon = tpl.iconID >= 0 ? `/icons/x4/${tpl.iconID}.png` : 'placeholder_item.png';
      const options = parseOptions(it.options);
      return {
        name: tpl.name,
        tempId: tempId,
        cost: parseInt(it.cost, 10) || 0,
        currency,
        icon,
        options
      };
    });
    // Clean tab name by removing any '<' or '>' characters to avoid HTML tags in DB
    const cleanName = (r.tab_name || '').replace(/[<>]/g, ' ');
    shop.tabs[r.tab_index] = {
      name: cleanName,
      items: parsedItems,
      rawItems,
      // Retain original shopId and tabIndex for client to identify the correct DB record when editing
      shopId: r.shop_id,
      tabIndex: r.tab_index
    };
  }
  shopsData = Array.from(map.values());
}

/**
 * Load players and their items from the database. Because this source stores
 * items as JSON in the player table (items_body and items_bag), we need to
 * decode those fields. The data_point field (JSON array) is used to
 * extract power, and gender is used as a stand‑in for level. You can
 * customise this function to suit your schema.
 */
async function loadPlayers() {
  // Fetch additional columns for tasks, side tasks and item time.  These JSON fields
  // will be parsed and returned to the client so that the UI can display
  // comprehensive player information beyond just items and power.  If these
  // columns do not exist in your schema they will simply be undefined in
  // the result set and the parsing below will safely fallback.
  // Include the head column to compute or assign avatar icons
  const [rows] = await dbPool.query('SELECT id, name, gender, head, data_point, items_body, items_bag, data_task, data_side_task, data_item_time FROM player');
  const players = [];
  for (const r of rows) {
    // Extract power from data_point JSON array
    let power = 0;
    try {
      const dp = JSON.parse(r.data_point || '[]');
      if (Array.isArray(dp) && dp.length > 1) {
        power = parseInt(dp[1], 10) || 0;
      }
    } catch (_) {}
    const player = {
      id: r.id,
      name: r.name,
      level: r.gender,
      power,
      items: [],
      stats: null,
      tasks: [],
      sideTasks: [],
      itemTimes: [],
      head: r.head !== undefined ? parseInt(r.head, 10) : null,
      // Always use a default avatar image as requested
      avatarIcon: '/placeholder_npc.png',
      hp: 0,
      mp: 0,
      hpBase: 0,
      mpBase: 0
    };
    // Helper to parse items from JSON string
    const parseItems = (jsonStr) => {
      const list = [];
      if (!jsonStr) return list;
      try {
        const arr = JSON.parse(jsonStr);
        if (Array.isArray(arr)) {
          for (const it of arr) {
            let tempId;
            let quantity = 1;
            let opts = [];
            if (Array.isArray(it)) {
              tempId = parseInt(it[0], 10);
              quantity = it.length > 1 ? parseInt(it[1], 10) : 1;
            } else if (it && typeof it === 'object') {
              tempId = parseInt(it.id || it.tempId || it.templateId, 10);
              quantity = parseInt(it.quantity || it.q || 1, 10);
              opts = parseOptions(it.options);
            }
            if (!isNaN(tempId)) {
              const tpl = itemTemplateMap[tempId] || { name: `Item ${tempId}`, iconID: -1 };
              list.push({
                name: tpl.name,
                tempId,
                quantity: isNaN(quantity) ? 1 : quantity,
                icon: tpl.iconID >= 0 ? `/icons/x4/${tpl.iconID}.png` : 'placeholder_item.png',
                options: opts
              });
            }
          }
        }
      } catch (_) {
        // ignore parse errors
      }
      return list;
    };
    const bodyItems = parseItems(r.items_body);
    const bagItems = parseItems(r.items_bag);
    player.items = bodyItems.concat(bagItems);
    // Parse raw data_point (stats).  This is expected to be a JSON array or
    // object.  If parsing fails, keep it as null.  Clients can display
    // this raw structure for debugging or further processing.
    try {
      if (r.data_point) {
        player.stats = JSON.parse(r.data_point);
        // If stats is an array, attempt to extract HP/MP and base values based on index
        if (Array.isArray(player.stats)) {
          const arr = player.stats;
          // arr[2] may represent current HP, arr[4] may represent current MP
          if (arr.length > 2 && !isNaN(parseInt(arr[2], 10))) {
            player.hp = parseInt(arr[2], 10);
          }
          if (arr.length > 4 && !isNaN(parseInt(arr[4], 10))) {
            player.mp = parseInt(arr[4], 10);
          }
          // arr[5] and arr[6] may represent base HP and MP
          if (arr.length > 5 && !isNaN(parseInt(arr[5], 10))) {
            player.hpBase = parseInt(arr[5], 10);
          }
          if (arr.length > 6 && !isNaN(parseInt(arr[6], 10))) {
            player.mpBase = parseInt(arr[6], 10);
          }
        }
      }
    } catch (_) {
      player.stats = null;
    }
    // Parse main tasks JSON.  This field is usually a JSON array of objects.
    try {
      if (r.data_task) {
        const arr = JSON.parse(r.data_task);
        if (Array.isArray(arr)) {
          player.tasks = arr;
        }
      }
    } catch (_) {
      player.tasks = [];
    }
    // Parse side tasks JSON.  Typically a JSON array of side task objects.
    try {
      if (r.data_side_task) {
        const arr = JSON.parse(r.data_side_task);
        if (Array.isArray(arr)) {
          player.sideTasks = arr;
        }
      }
    } catch (_) {
      player.sideTasks = [];
    }
    // Parse item time data.  Some servers store timed items or buffs in this field.
    try {
      if (r.data_item_time) {
        const arr = JSON.parse(r.data_item_time);
        if (Array.isArray(arr)) {
          player.itemTimes = arr;
        }
      }
    } catch (_) {
      player.itemTimes = [];
    }
    players.push(player);
  }
  playersData = players;
}

/**
 * Write updated items back to the tab_shop table. When the front‑end
 * modifies items in a tab (add or edit), we convert our friendly
 * representation back to the DB schema (temp_id, cost, type_sell,
 * item_spec, is_new, is_sell, options) and update the JSON in the
 * database. Note: this function expects the caller to pass shopId and
 * tabIndex rather than array indexes.
 *
 * @param {number} shopId  Id of the shop (primary key of `shop` table)
 * @param {number} tabIndex Index of the tab (tab_index column)
 * @param {Array<Object>} items Array of raw items (DB schema) to save
 */
async function updateTabItems(shopId, tabIndex, items) {
  await dbPool.query(
    'UPDATE tab_shop SET items = ? WHERE shop_id = ? AND tab_index = ?',
    [JSON.stringify(items), shopId, tabIndex]
  );
}

/**
 * Serve static assets from the web directory. For icons under /icons/x4,
 * we serve files from the same root web directory so that NPC and item
 * icons can be displayed. If a file does not exist we send 404.
 */
function serveStatic(req, res) {
  let filePath = req.url;
  // Remove leading slash
  if (filePath.startsWith('/')) filePath = filePath.slice(1);
  // Default to index.html
  if (filePath === '') filePath = 'index.html';
  const fullPath = path.join(WEB_DIR, filePath);
  // Prevent directory traversal
  if (!fullPath.startsWith(WEB_DIR)) {
    res.statusCode = 403;
    res.end('Forbidden');
    return;
  }
  fs.promises.readFile(fullPath)
    .then(data => {
      const ext = path.extname(fullPath).toLowerCase();
      const mime = ext === '.js' ? 'application/javascript'
        : ext === '.css' ? 'text/css'
        : ext === '.png' ? 'image/png'
        : ext === '.json' ? 'application/json'
        : 'text/html';
      res.statusCode = 200;
      res.setHeader('Content-Type', mime);
      res.end(data);
    })
    .catch(() => {
      res.statusCode = 404;
      res.end('Not found');
    });
}

/**
 * Main API handler. Dispatches requests based on path and method. All
 * endpoints return JSON. On errors we respond with appropriate status
 * codes and messages. See README for details of each endpoint.
 */
async function handleApi(req, res) {
  const url = req.url;
  const method = req.method;
  // GET /api/shops
  if (method === 'GET' && url === '/api/shops') {
    res.setHeader('Content-Type', 'application/json');
    // Do not send rawItems to client
    const cleanShops = shopsData.map(shop => {
      // Remove undefined/null tabs to prevent client errors and include original identifiers
      const definedTabs = shop.tabs.filter(tab => !!tab);
      return {
        shopId: shop.shopId,
        npcId: shop.npcId,
        npcName: shop.npcName,
        npcIcon: shop.npcIcon,
        tabs: definedTabs.map(tab => ({
          name: tab.name,
          items: tab.items,
          // include shopId and tabIndex for client to know DB record
          shopId: tab.shopId,
          tabIndex: tab.tabIndex
        }))
      };
    });
    res.end(JSON.stringify(cleanShops));
    return;
  }
  // GET /api/players
  if (method === 'GET' && url === '/api/players') {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(playersData));
    return;
  }
  // GET /api/item_templates
  if (method === 'GET' && url === '/api/item_templates') {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(itemTemplates));
    return;
  }
  // GET /api/item_option_templates
  if (method === 'GET' && url === '/api/item_option_templates') {
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(itemOptionTemplates));
    return;
  }

  // GET /api/giftcodes
  if (method === 'GET' && url === '/api/giftcodes') {
    // List all giftcodes with parsed items. The giftcode table is expected to
    // have columns code, count_left, detail, datecreate, expired and type.
    dbPool.query('SELECT code, count_left, detail, datecreate, expired, `type` FROM giftcode')
      .then(([rows]) => {
        const list = [];
        for (const r of rows) {
          let parsedItems = [];
          if (r.detail) {
            try {
              const arr = JSON.parse(r.detail);
              if (Array.isArray(arr)) {
                parsedItems = arr.map(it => {
                  const tempId = parseInt(it.temp_id ?? it.tempId, 10);
                  const quantity = parseInt(it.quantity ?? 1, 10) || 1;
                  const tpl = itemTemplateMap[tempId] || { name: `Item ${tempId}`, iconID: -1 };
                  // parse raw options
                  const opts = parseOptions(it.options);
                  // map options to descriptions
                  const detailedOpts = opts.map(o => {
                    const descTpl = optionTemplateMap[o.id];
                    let description;
                    if (descTpl) {
                      if (descTpl.includes('#')) {
                        // replace all '#' with param
                        description = descTpl.replace(/#/g, o.param);
                      } else {
                        description = descTpl + ' ' + o.param;
                      }
                    } else {
                      description = `Option ${o.id}: +${o.param}`;
                    }
                    return { id: o.id, param: o.param, description };
                  });
                  const iconPath = tpl.iconID >= 0 ? `/icons/x4/${tpl.iconID}.png` : 'placeholder_item.png';
                  return {
                    name: tpl.name,
                    temp_id: tempId,
                    quantity,
                    icon: iconPath,
                    options: detailedOpts
                  };
                });
              }
            } catch (e) {
              // ignore parse errors
            }
          }
          list.push({
            code: r.code,
            countLeft: r.count_left,
            type: r.type,
            datecreate: r.datecreate,
            expired: r.expired,
            items: parsedItems
          });
        }
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify(list));
      })
      .catch(err => {
        console.error(err);
        res.statusCode = 500;
        res.end(JSON.stringify({ error: 'Failed to load giftcodes' }));
      });
    return;
  }
  // POST /api/giftcodes
  if (method === 'POST' && url === '/api/giftcodes') {
    // Create a giftcode entry. Expected payload: {
    //   code: string,
    //   items: array,
    //   countLeft: number,
    //   type: number (0 or 1),
    //   expired: string (date in YYYY-MM-DD format or ISO string)
    // }
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 1e6) req.connection.destroy();
    });
    req.on('end', async () => {
      try {
        const data = JSON.parse(body);
        // Validate mandatory fields
        if (!data.code || !Array.isArray(data.items) || data.items.length === 0) {
          res.statusCode = 400;
          res.end(JSON.stringify({ error: 'Invalid giftcode data' }));
          return;
        }
        const countLeft = parseInt(data.countLeft, 10);
        const typeVal = parseInt(data.type, 10);
        const expired = data.expired ? new Date(data.expired) : null;
        const now = new Date();
        // Transform items into DB format: temp_id and quantity keys
        const itemsForDb = Array.isArray(data.items) ? data.items.map(it => {
          const tempId = parseInt(it.temp_id ?? it.tempId, 10);
          const qty = parseInt(it.quantity ?? 1, 10);
          const opts = Array.isArray(it.options) ? it.options.map(o => ({ id: o.id, param: o.param })) : [];
          return {
            temp_id: isNaN(tempId) ? null : tempId,
            quantity: isNaN(qty) ? 1 : qty,
            options: opts
          };
        }) : [];
        await dbPool.query(
          'INSERT INTO giftcode (code, count_left, detail, datecreate, expired, `type`) VALUES (?, ?, ?, ?, ?, ?)',
          [
            data.code,
            isNaN(countLeft) ? 1 : countLeft,
            JSON.stringify(itemsForDb),
            now,
            expired,
            isNaN(typeVal) ? 0 : typeVal
          ]
        );
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify({ success: true }));
      } catch (e) {
        console.error(e);
        res.statusCode = 500;
        res.end(JSON.stringify({ error: 'Failed to save giftcode' }));
      }
    });
    return;
  }
  // Add new item: POST /api/shops/{shopId}/tabs/{tabIndex}/items
  const addItemMatch = url.match(/^\/api\/shops\/(\d+)\/tabs\/(\d+)\/items$/);
  if (method === 'POST' && addItemMatch) {
    const shopId = parseInt(addItemMatch[1], 10);
    const tabIndex = parseInt(addItemMatch[2], 10);
    const shop = shopsData.find(s => s.shopId === shopId);
    if (!shop || !shop.tabs[tabIndex]) {
      res.statusCode = 400;
      res.end(JSON.stringify({ error: 'Invalid shop or tab index' }));
      return;
    }
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 1e6) req.connection.destroy();
    });
    req.on('end', async () => {
      try {
        const newItem = JSON.parse(body);
        // Validate payload: tempId, cost, currency
        if (!newItem || typeof newItem.tempId !== 'number') {
          res.statusCode = 400;
          res.end(JSON.stringify({ error: 'Invalid item data' }));
          return;
        }
        // Determine type_sell from currency string
        const currencyMap = { 'vàng': 0, 'ngọc': 1, 'ruby': 3, 'coupon': 4 };
        const typeSell = currencyMap[newItem.currency] ?? 0;
        const rawItem = {
          temp_id: newItem.tempId,
          cost: newItem.cost || 0,
          item_spec: 0,
          type_sell: typeSell,
          is_new: false,
          is_sell: true,
          options: (newItem.options || []).map(o => ({ id: o.id, param: o.param }))
        };
        // Append to cached structures
        shop.tabs[tabIndex].items.push({
          name: itemTemplateMap[newItem.tempId]?.name || `Item ${newItem.tempId}`,
          tempId: newItem.tempId,
          cost: newItem.cost || 0,
          currency: newItem.currency || 'vàng',
          icon: itemTemplateMap[newItem.tempId]?.iconID >= 0 ? `/icons/x4/${itemTemplateMap[newItem.tempId].iconID}.png` : 'placeholder_item.png',
          options: newItem.options || []
        });
        shop.tabs[tabIndex].rawItems.push(rawItem);
        // Persist to DB
        await updateTabItems(shopId, tabIndex, shop.tabs[tabIndex].rawItems);
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify({ success: true }));
      } catch (e) {
        console.error(e);
        res.statusCode = 500;
        res.end(JSON.stringify({ error: 'Failed to add item' }));
      }
    });
    return;
  }
  // Update existing item: PUT /api/shops/{shopId}/tabs/{tabIndex}/items/{itemIndex}
  const updateMatch = url.match(/^\/api\/shops\/(\d+)\/tabs\/(\d+)\/items\/(\d+)$/);
  if (updateMatch && (method === 'PUT' || method === 'POST')) {
    const shopId = parseInt(updateMatch[1], 10);
    const tabIndex = parseInt(updateMatch[2], 10);
    const itemIndex = parseInt(updateMatch[3], 10);
    const shop = shopsData.find(s => s.shopId === shopId);
    if (!shop || !shop.tabs[tabIndex] || !shop.tabs[tabIndex].items[itemIndex]) {
      res.statusCode = 400;
      res.end(JSON.stringify({ error: 'Invalid shop, tab or item index' }));
      return;
    }
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 1e6) req.connection.destroy();
    });
    req.on('end', async () => {
      try {
        const updates = JSON.parse(body);
        const item = shop.tabs[tabIndex].items[itemIndex];
        const rawItem = shop.tabs[tabIndex].rawItems[itemIndex];
        // Update cost
        if (updates.cost !== undefined) {
          item.cost = parseInt(updates.cost, 10);
          rawItem.cost = parseInt(updates.cost, 10);
        }
        // Update currency/type_sell
        if (updates.currency) {
          item.currency = updates.currency;
          const currencyMap = { 'vàng': 0, 'ngọc': 1, 'ruby': 3, 'coupon': 4 };
          rawItem.type_sell = currencyMap[updates.currency] ?? 0;
        }
        // Update options
        if (Array.isArray(updates.options)) {
          item.options = updates.options.map(o => ({ id: o.id, param: o.param }));
          rawItem.options = updates.options.map(o => ({ id: o.id, param: o.param }));
        }
        // Persist changes
        await updateTabItems(shopId, tabIndex, shop.tabs[tabIndex].rawItems);
        res.setHeader('Content-Type', 'application/json');
        res.end(JSON.stringify({ success: true }));
      } catch (e) {
        console.error(e);
        res.statusCode = 500;
        res.end(JSON.stringify({ error: 'Failed to update item' }));
      }
    });
    return;
  }
  
// PUT /api/shops/{shopId}
// This endpoint allows updating a shop's NPC assignment and tab names.
// Payload: { npcId?: number, tabs?: [{ index: number, name: string }] }
const updateShopMatch = url.match(/^\/api\/shops\/(\d+)$/);
if (method === 'PUT' && updateShopMatch) {
  const shopIdNum = parseInt(updateShopMatch[1], 10);
  const shopObj = shopsData.find(s => s.shopId === shopIdNum);
  if (!shopObj) {
    res.statusCode = 400;
    res.end(JSON.stringify({ error: 'Invalid shop id' }));
    return;
  }
  let bodyStr = '';
  req.on('data', chunk => {
    bodyStr += chunk;
    if (bodyStr.length > 1e6) req.connection.destroy();
  });
  req.on('end', async () => {
    try {
      const json = bodyStr ? JSON.parse(bodyStr) : {};
      // Update NPC
      if (json.npcId !== undefined && !isNaN(parseInt(json.npcId, 10)) && parseInt(json.npcId, 10) !== shopObj.npcId) {
        const newNpcId = parseInt(json.npcId, 10);
        await dbPool.query('UPDATE shop SET npc_id = ? WHERE id = ?', [newNpcId, shopIdNum]);
        shopObj.npcId = newNpcId;
        const npcInfo = npcTemplateMap[newNpcId] || { name: `NPC ${newNpcId}`, iconID: -1 };
        shopObj.npcName = npcInfo.name;
        shopObj.npcIcon = npcInfo.iconID >= 0 ? `/icons/x4/${npcInfo.iconID}.png` : 'placeholder_npc.png';
      }
      // Update tab names
      if (Array.isArray(json.tabs)) {
        for (const tabData of json.tabs) {
          const idx = parseInt(tabData.index, 10);
          const name = typeof tabData.name === 'string' ? tabData.name : null;
          if (!isNaN(idx) && name) {
            const tabObj = shopObj.tabs[idx];
            if (tabObj) {
              const clean = name.replace(/[<>]/g, ' ');
              await dbPool.query('UPDATE tab_shop SET tab_name = ? WHERE shop_id = ? AND tab_index = ?', [clean, shopIdNum, idx]);
              tabObj.name = clean;
            }
          }
        }
      }
      res.setHeader('Content-Type', 'application/json');
      res.end(JSON.stringify({ success: true }));
    } catch (e) {
      console.error(e);
      res.statusCode = 500;
      res.end(JSON.stringify({ error: 'Failed to update shop' }));
    }
  });
  return;
}

// No matching API route
  res.statusCode = 404;
  res.end(JSON.stringify({ error: 'Not found' }));
}

/**
 * Create and start the HTTP server. Static files are served from the web
 * directory. API routes are prefixed with /api. On startup we preload
 * all data from the database.
 */
async function start() {
  await initDbPool();
  await loadNpcTemplates();
  await loadItemTemplates();
  await loadItemOptionTemplates();
  await loadShops();
  await loadPlayers();
  const server = http.createServer((req, res) => {
    if (req.url.startsWith('/api/')) {
      handleApi(req, res);
    } else {
      serveStatic(req, res);
    }
  });
  const port = 3000;
  server.listen(port, () => {
    console.log(`Server running at http://localhost:${port}`);
  });
}

start().catch(err => {
  console.error('Failed to start server:', err);
});