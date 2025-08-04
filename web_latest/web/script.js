let shopsData = [];
let currentShopIndex = null;
let currentTabIndex = 0;

// Data caches for players, item templates, option templates
let playersData = [];
let itemTemplates = [];
let optionTemplates = [];
let giftcodesData = [];

/**
 * Tải dữ liệu shop từ API. Nếu thành công, lưu vào biến shopsData
 * và hiển thị danh sách NPC. Nếu lỗi, hiển thị thông báo.
 */
function loadShopsFromApi() {
  fetch('/api/shops')
    .then(response => response.json())
    .then(async data => {
      // If option templates have not been loaded yet, fetch them now so that
      // item options can be rendered properly. Only fetch once.
      if (!optionTemplates || optionTemplates.length === 0) {
        try {
          const opts = await fetch('/api/item_option_templates').then(res => res.json());
          optionTemplates = opts;
        } catch (e) {
          console.error('Lỗi tải option templates:', e);
        }
      }
      // Group shops by NPC ID to avoid duplicate NPC entries. Each tab will retain
      // its original shopId and tabIndex to allow edits and inserts to map back to DB.
      const groupMap = {};
      data.forEach(shop => {
        if (!groupMap[shop.npcId]) {
          groupMap[shop.npcId] = {
            npcId: shop.npcId,
            npcName: shop.npcName,
            npcIcon: shop.npcIcon,
            tabs: []
          };
        }
        shop.tabs.forEach((tab, idx) => {
          if (!tab) return;
          groupMap[shop.npcId].tabs.push({
            name: tab.name,
            items: tab.items,
            shopId: tab.shopId !== undefined ? tab.shopId : shop.shopId,
            tabIndex: tab.tabIndex !== undefined ? tab.tabIndex : idx
          });
        });
      });
      shopsData = Object.values(groupMap);
      renderNpcList();
    })
    .catch(err => {
      console.error('Lỗi tải dữ liệu shop:', err);
      document.getElementById('npc-list').innerHTML = '<p>Lỗi tải dữ liệu shop</p>';
    });
}

// Render the list of NPCs on the left
function renderNpcList() {
  const npcListEl = document.getElementById('npc-list');
  npcListEl.innerHTML = '';
  shopsData.forEach((shop, index) => {
    const npcItem = document.createElement('div');
    npcItem.className = 'npc-item';
    npcItem.innerHTML = `<img src="${shop.npcIcon}" alt="${shop.npcName}"><span>${shop.npcName}</span>`;
    npcItem.addEventListener('click', () => {
      currentShopIndex = index;
      currentTabIndex = 0;
      renderShop(shop);
    });
    npcListEl.appendChild(npcItem);
  });
}

// Render the selected shop
function renderShop(shop) {
  // Update title
  const titleEl = document.getElementById('shop-title');
  titleEl.textContent = `${shop.npcName} - Shop`;

  // Render tabs
  const tabsEl = document.getElementById('tabs');
  tabsEl.innerHTML = '';
  shop.tabs.forEach((tab, index) => {
    if (!tab) return;
    const tabEl = document.createElement('div');
    tabEl.className = 'tab' + (index === currentTabIndex ? ' active' : '');
    tabEl.textContent = tab.name;
    tabEl.addEventListener('click', () => {
      currentTabIndex = index;
      renderItems(shop.tabs[index]);
      highlightTabs(tabsEl, index);
    });
    tabsEl.appendChild(tabEl);
  });

  // Render items of the first tab
  renderItems(shop.tabs[currentTabIndex]);
}

// Highlight the active tab
function highlightTabs(tabsEl, activeIndex) {
  Array.from(tabsEl.children).forEach((child, idx) => {
    if (idx === activeIndex) {
      child.classList.add('active');
    } else {
      child.classList.remove('active');
    }
  });
}

// Render items in the selected tab
function renderItems(tab) {
  const itemsContainer = document.getElementById('items-container');
  itemsContainer.innerHTML = '';
  tab.items.forEach((item, itemIndex) => {
    const card = document.createElement('div');
    card.className = 'item-card';
    let optionsHtml = '';
    if (item.options && item.options.length > 0) {
      // Build a map from option ID to its description
      const optMap = {};
      optionTemplates.forEach(o => { optMap[o.id] = o.description; });
      optionsHtml = '<div class="options">' + item.options.map(opt => {
        const tpl = optMap[opt.id];
        if (tpl) {
          if (tpl.includes('#')) {
            return tpl.replace(/#/g, opt.param);
          } else {
            return tpl + ' +' + opt.param;
          }
        }
        return `Option ${opt.id}: +${opt.param}`;
      }).join('<br>') + '</div>';
    }
    card.innerHTML = `
      <img src="${item.icon}" alt="${item.name}" class="mx-auto w-12 h-12" />
      <div class="item-name">${item.name}</div>
      <div class="item-cost">Giá: ${item.cost} ${item.currency}</div>
      ${optionsHtml}
      <button class="edit-item-btn" data-item-index="${itemIndex}">Chỉnh sửa</button>
    `;
    itemsContainer.appendChild(card);
  });
  // Gắn sự kiện cho các nút chỉnh sửa
  document.querySelectorAll('.edit-item-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const itemIndex = parseInt(btn.getAttribute('data-item-index'), 10);
      showEditItemDialog(itemIndex);
    });
  });
  // Sau khi hiển thị các item, hiển thị form thêm item nếu có tab
  renderAddItemForm();
}

/**
 * Hiển thị form thêm item dưới danh sách item của tab hiện tại.
 * Form gồm các trường: name, tempId, cost, currency và options. Khi nhấn
 * nút "Thêm", gửi yêu cầu POST tới API. Sau khi thành công, tải lại
 * dữ liệu shop và cập nhật UI.
 */
function renderAddItemForm() {
  const container = document.getElementById('shop-content');
  // Xóa form cũ nếu có
  let existingForm = document.getElementById('add-item-form');
  if (existingForm) existingForm.remove();
  // Không hiển thị nếu chưa chọn shop hoặc tab
  if (currentShopIndex === null || !shopsData[currentShopIndex] ||
      !shopsData[currentShopIndex].tabs[currentTabIndex]) {
    return;
  }
  const form = document.createElement('div');
  form.id = 'add-item-form';
  form.style.marginTop = '20px';
  form.style.padding = '10px';
  form.style.borderTop = '1px solid #ddd';
  // Trước khi xây dựng form, nếu danh sách optionTemplates rỗng thì tải từ API
  const loadOptions = optionTemplates.length === 0
    ? fetch('/api/item_option_templates').then(res => res.json()).then(data => { optionTemplates = data; })
    : Promise.resolve();
  loadOptions.then(() => {
    // Build options selector HTML
    let optionsHtml = '';
    optionTemplates.forEach(opt => {
      optionsHtml += `
        <div style="margin-bottom:6px;">
          <label><input type="checkbox" class="item-opt-checkbox" value="${opt.id}"> ${opt.description}</label>
          <input type="number" class="item-opt-param" placeholder="param" style="width:60px; margin-left:4px;" disabled>
        </div>
      `;
    });
    form.innerHTML = `
      <h3>Thêm item vào tab</h3>
      <div style="display:flex; flex-wrap: wrap; gap:10px; align-items: flex-end;">
        <div style="flex:1; min-width:160px;">
          <label>Tên:</label><br>
          <input type="text" id="item-name" style="width:100%;">
        </div>
        <div style="flex:1; min-width:160px;">
          <label>Temp ID:</label><br>
          <input type="number" id="item-tempId" style="width:100%;">
        </div>
        <div style="flex:1; min-width:160px;">
          <label>Giá:</label><br>
          <input type="number" id="item-cost" style="width:100%;">
        </div>
        <div style="flex:1; min-width:160px;">
          <label>Loại tiền:</label><br>
          <select id="item-currency" style="width:100%;">
            <option value="vàng">Vàng</option>
            <option value="ngọc">Ngọc</option>
            <option value="ruby">Ruby</option>
            <option value="coupon">Coupon</option>
          </select>
        </div>
      </div>
      <div style="margin-top:10px;">
        <label>Chọn options và nhập param:</label>
        <div id="item-options-container" style="margin-top:6px; max-height:120px; overflow-y:auto; border:1px solid #ddd; padding:4px;">${optionsHtml}</div>
      </div>
      <button id="add-item-button" style="margin-top:10px;">Thêm</button>
      <p id="add-item-message" style="color:#c00;"></p>
    `;
    container.appendChild(form);
    // Kích hoạt/khóa input param khi checkbox thay đổi
    form.querySelectorAll('.item-opt-checkbox').forEach(cb => {
      cb.addEventListener('change', () => {
        // tìm ô nhập param trong cùng container <div>
        const containerDiv = cb.closest('div');
        const paramInput = containerDiv.querySelector('.item-opt-param');
        if (!paramInput) return;
        paramInput.disabled = !cb.checked;
        if (!cb.checked) paramInput.value = '';
      });
    });
    // Xử lý nút thêm
    document.getElementById('add-item-button').addEventListener('click', () => {
      const name = document.getElementById('item-name').value.trim();
      const tempId = parseInt(document.getElementById('item-tempId').value, 10);
      const cost = parseInt(document.getElementById('item-cost').value, 10);
      const currency = document.getElementById('item-currency').value;
      const msgEl = document.getElementById('add-item-message');
      if (!name || isNaN(tempId) || isNaN(cost)) {
        msgEl.textContent = 'Vui lòng nhập đầy đủ tên, tempId và giá.';
        return;
      }
      // Thu thập options đã chọn
      const options = [];
      const checkboxes = form.querySelectorAll('.item-opt-checkbox');
      checkboxes.forEach(cb => {
        if (cb.checked) {
          const id = parseInt(cb.value, 10);
          const containerDiv = cb.closest('div');
          const paramInput = containerDiv.querySelector('.item-opt-param');
          const param = parseInt(paramInput ? paramInput.value : '', 10);
          if (!isNaN(id) && !isNaN(param)) {
            options.push({ id, param });
          }
        }
      });
      const newItem = {
        name,
        tempId,
        cost,
        currency,
        icon: 'placeholder_item.png',
        options
      };
      // Use shopId from shopsData instead of array index for API path
      // Use the original shopId and tabIndex stored in the tab object
      const tabObj = shopsData[currentShopIndex].tabs[currentTabIndex];
      const shopId = tabObj.shopId;
      const tabIdx = tabObj.tabIndex;
      fetch(`/api/shops/${shopId}/tabs/${tabIdx}/items`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newItem)
      })
        .then(resp => resp.json())
        .then(data => {
          if (data.success) {
            msgEl.style.color = '#090';
            msgEl.textContent = 'Thêm item thành công!';
            document.getElementById('item-name').value = '';
            document.getElementById('item-tempId').value = '';
            document.getElementById('item-cost').value = '';
            // Reset options form
            form.querySelectorAll('.item-opt-checkbox').forEach(cb => cb.checked = false);
            form.querySelectorAll('.item-opt-param').forEach(inp => { inp.disabled = true; inp.value = ''; });
            loadShopsFromApi();
          } else {
            msgEl.style.color = '#c00';
            msgEl.textContent = data.error || 'Có lỗi xảy ra.';
          }
        })
        .catch(err => {
          msgEl.style.color = '#c00';
          msgEl.textContent = 'Có lỗi xảy ra khi gửi yêu cầu.';
          console.error(err);
        });
    });
  });
}

/**
 * Hiển thị hộp thoại chỉnh sửa item. Cho phép sửa giá, loại tiền và options.
 * Sau khi cập nhật thành công, nạp lại dữ liệu shop.
 * @param {number} itemIndex
 */
function showEditItemDialog(itemIndex) {
  const shopIdx = currentShopIndex;
  const tabIdx = currentTabIndex;
  const shop = shopsData[shopIdx];
  const tab = shop.tabs[tabIdx];
  const item = tab.items[itemIndex];
  if (!item) return;
  // Create modal overlay
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  // Create modal box
  const modal = document.createElement('div');
  modal.className = 'modal-box';
  // Build options selector for editing
  // Ensure optionTemplates is loaded
  const buildOptionRows = () => {
    return optionTemplates.map(opt => {
      // Check if this option exists in current item
      const existing = (item.options || []).find(o => o.id === opt.id);
      const checked = existing ? 'checked' : '';
      const paramVal = existing ? existing.param : '';
      return `
        <div class="edit-option-row" style="margin-bottom:6px;">
          <label><input type="checkbox" class="edit-opt-checkbox" value="${opt.id}" ${checked}> ${opt.description}</label>
          <input type="number" class="edit-opt-param" placeholder="param" style="width:60px; margin-left:4px;" ${checked ? '' : 'disabled'} value="${paramVal}">
        </div>
      `;
    }).join('');
  };
  modal.innerHTML = `
    <h3 class="mb-2 font-bold text-lg">Chỉnh sửa Item</h3>
    <div style="margin-bottom:10px;">
      <label>Giá (vàng):</label><br>
      <input type="number" id="edit-cost" style="width:100%;" value="${item.cost}">
    </div>
    <div style="margin-bottom:10px;">
      <label>Loại tiền:</label><br>
      <select id="edit-currency" style="width:100%;">
        <option value="vàng" ${item.currency === 'vàng' ? 'selected' : ''}>Vàng</option>
        <option value="ngọc" ${item.currency === 'ngọc' ? 'selected' : ''}>Ngọc</option>
        <option value="ruby" ${item.currency === 'ruby' ? 'selected' : ''}>Ruby</option>
        <option value="coupon" ${item.currency === 'coupon' ? 'selected' : ''}>Coupon</option>
      </select>
    </div>
    <div style="margin-bottom:10px;">
      <label>Chỉnh sửa options và param:</label>
      <div id="edit-options" style="max-height:150px; overflow-y:auto; border:1px solid #ddd; padding:4px;">
        ${buildOptionRows()}
      </div>
    </div>
    <div style="display:flex; justify-content:flex-end; gap:8px;">
      <button id="cancel-edit" class="bg-gray-300 px-3 py-1 rounded">Hủy</button>
      <button id="save-edit" class="bg-blue-600 text-white px-3 py-1 rounded">Cập nhật</button>
    </div>
  `;
  overlay.appendChild(modal);
  document.body.appendChild(overlay);
  // Add interactions to options checkboxes
  modal.querySelectorAll('.edit-opt-checkbox').forEach(cb => {
    cb.addEventListener('change', () => {
      const containerDiv = cb.closest('.edit-option-row');
      const paramInput = containerDiv.querySelector('.edit-opt-param');
      if (!paramInput) return;
      paramInput.disabled = !cb.checked;
      if (!cb.checked) paramInput.value = '';
    });
  });
  // Cancel button
  modal.querySelector('#cancel-edit').addEventListener('click', () => {
    document.body.removeChild(overlay);
  });
  // Save button
  modal.querySelector('#save-edit').addEventListener('click', () => {
    const newCost = parseInt(modal.querySelector('#edit-cost').value, 10);
    const newCurrency = modal.querySelector('#edit-currency').value;
    const updates = {};
    if (!isNaN(newCost) && newCost !== item.cost) updates.cost = newCost;
    if (newCurrency && newCurrency !== item.currency) updates.currency = newCurrency;
    // Collect options
    const opts = [];
    modal.querySelectorAll('.edit-opt-checkbox').forEach(cb => {
      if (cb.checked) {
        const id = parseInt(cb.value, 10);
        const paramInput = cb.closest('.edit-option-row').querySelector('.edit-opt-param');
        const param = parseInt(paramInput.value, 10);
        if (!isNaN(id) && !isNaN(param)) {
          opts.push({ id, param });
        }
      }
    });
    if (opts.length > 0) updates.options = opts;
    // Only update if something changed
    if (Object.keys(updates).length > 0) {
      const shopId = tab.shopId;
      const originalTabIndex = tab.tabIndex;
      fetch(`/api/shops/${shopId}/tabs/${originalTabIndex}/items/${itemIndex}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(updates)
      })
        .then(resp => resp.json())
        .then(data => {
          if (data.success) {
            // Close modal and reload shops
            document.body.removeChild(overlay);
            loadShopsFromApi();
          } else {
            alert(data.error || 'Có lỗi xảy ra');
          }
        })
        .catch(err => {
          alert('Có lỗi khi gửi yêu cầu');
          console.error(err);
        });
    } else {
      // Nothing changed, just close modal
      document.body.removeChild(overlay);
    }
  });
}

/**
 * Tải dữ liệu người chơi và hiển thị trong tab Players. Mỗi người chơi hiển thị
 * thông tin cơ bản và danh sách vật phẩm họ sở hữu.
 */
function loadPlayers() {
  fetch('/api/players')
    .then(resp => resp.json())
    .then(data => {
      playersData = data;
      renderPlayers();
    })
    .catch(err => {
      document.getElementById('players-list').innerHTML = '<p>Lỗi tải danh sách người chơi.</p>';
      console.error(err);
    });
}

function renderPlayers() {
  const listEl = document.getElementById('players-list');
  // Clear previous contents
  listEl.innerHTML = '';
  // Loop through each player and build a card with summary and expandable details
  playersData.forEach(player => {
    const card = document.createElement('div');
    // Use Tailwind classes for styling: white background, border, rounded corners and shadow
    card.className = 'bg-white border border-gray-300 rounded p-4 shadow-sm mb-4';
    // Build stats representation.  If stats is an array, list each index and value;
    // if an object, list key-value pairs; otherwise show as string.
    let statsHtml = '';
    if (player.stats) {
      if (Array.isArray(player.stats)) {
        statsHtml = '<ul class="list-disc list-inside text-sm text-gray-700">' + player.stats.map((val, idx) => `<li>Chỉ số ${idx}: ${val}</li>`).join('') + '</ul>';
      } else if (typeof player.stats === 'object') {
        statsHtml = '<ul class="list-disc list-inside text-sm text-gray-700">' + Object.entries(player.stats).map(([k, v]) => `<li>${k}: ${v}</li>`).join('') + '</ul>';
      } else {
        statsHtml = `<span class="text-sm text-gray-700">${String(player.stats)}</span>`;
      }
    }
    // Build items grid.  Each item will display its icon, name, quantity and options
    let itemsGrid = '';
    if (player.items && player.items.length > 0) {
      itemsGrid = '<div class="grid grid-cols-2 md:grid-cols-3 gap-2">' + player.items.map(it => {
        const optsDesc = (it.options || []).map(opt => `Option ${opt.id}: +${opt.param}`).join(', ');
        return `
          <div class="flex items-start p-2 border border-gray-200 rounded">
            <img src="${it.icon}" alt="${it.name}" class="w-8 h-8 mr-2">
            <div class="text-sm">
              <div class="font-medium">${it.name}</div>
              <div class="text-gray-600">x${it.quantity}</div>
              ${optsDesc ? `<div class="text-xs text-gray-500">${optsDesc}</div>` : ''}
            </div>
          </div>
        `;
      }).join('') + '</div>';
    }
    // Build tasks and side tasks HTML.  We iterate through each task object and display
    // a pretty-printed JSON representation.  You can further customise this to
    // display meaningful fields (e.g. task name or status).
    let tasksHtml = '';
    if (player.tasks && player.tasks.length > 0) {
      tasksHtml = '<ul class="list-disc list-inside text-sm text-gray-700">' + player.tasks.map(t => `<li><pre class="whitespace-pre-wrap">${JSON.stringify(t, null, 2)}</pre></li>`).join('') + '</ul>';
    }
    let sideTasksHtml = '';
    if (player.sideTasks && player.sideTasks.length > 0) {
      sideTasksHtml = '<ul class="list-disc list-inside text-sm text-gray-700">' + player.sideTasks.map(t => `<li><pre class="whitespace-pre-wrap">${JSON.stringify(t, null, 2)}</pre></li>`).join('') + '</ul>';
    }
    let itemTimesHtml = '';
    if (player.itemTimes && player.itemTimes.length > 0) {
      itemTimesHtml = `<pre class="whitespace-pre-wrap text-sm text-gray-700">${JSON.stringify(player.itemTimes, null, 2)}</pre>`;
    }
    // Compose the HTML structure with a summary header and a collapsible details section
    card.innerHTML = `
      <div class="flex items-center">
        <img src="${player.avatarIcon || 'placeholder_npc.png'}" alt="${player.name}" class="w-12 h-12 rounded-full mr-4">
        <div class="flex-1">
          <div class="font-semibold text-lg">${player.name}</div>
          <div class="text-sm text-gray-700">Level ${player.level} | Power ${player.power}</div>
          <div class="text-sm text-gray-700">HP ${player.hp}/${player.hpBase} | MP ${player.mp}/${player.mpBase}</div>
        </div>
      </div>
      <button class="toggle-details-btn mt-2 text-blue-600 hover:underline text-sm">Xem chi tiết</button>
      <div class="player-details mt-2 hidden">
        ${statsHtml ? `<div class="mb-2"><span class="font-semibold">Chỉ số:</span>${statsHtml}</div>` : ''}
        ${tasksHtml ? `<div class="mb-2"><span class="font-semibold">Nhiệm vụ:</span>${tasksHtml}</div>` : ''}
        ${sideTasksHtml ? `<div class="mb-2"><span class="font-semibold">Nhiệm vụ phụ:</span>${sideTasksHtml}</div>` : ''}
        ${itemTimesHtml ? `<div class="mb-2"><span class="font-semibold">Buffs:</span>${itemTimesHtml}</div>` : ''}
        ${itemsGrid ? `<div class="mb-2"><span class="font-semibold">Vật phẩm:</span>${itemsGrid}</div>` : ''}
      </div>
    `;
    // Attach click handler to toggle details visibility
    const toggleBtn = card.querySelector('.toggle-details-btn');
    const detailsEl = card.querySelector('.player-details');
    toggleBtn.addEventListener('click', () => {
      const isHidden = detailsEl.classList.contains('hidden');
      if (isHidden) {
        detailsEl.classList.remove('hidden');
        toggleBtn.textContent = 'Ẩn chi tiết';
      } else {
        detailsEl.classList.add('hidden');
        toggleBtn.textContent = 'Xem chi tiết';
      }
    });
    listEl.appendChild(card);
  });
}

/**
 * Tải dữ liệu item templates và option templates rồi dựng form tạo giftcode.
 */
function loadGiftcodeForm() {
  Promise.all([
    fetch('/api/item_templates').then(res => res.json()),
    fetch('/api/item_option_templates').then(res => res.json())
  ]).then(([items, options]) => {
    itemTemplates = items;
    optionTemplates = options;
    renderGiftcodeForm();
  }).catch(err => {
    document.getElementById('giftcode-form-container').innerHTML = '<p>Lỗi tải dữ liệu giftcode.</p>';
    console.error(err);
  });
}

function renderGiftcodeForm() {
  const container = document.getElementById('giftcode-form-container');
  container.innerHTML = '';
  const form = document.createElement('div');
  form.id = 'giftcode-form';
  form.style.border = '1px solid #ddd';
  form.style.borderRadius = '4px';
  form.style.padding = '10px';
  // Build select for items
  const itemOptionsHtml = itemTemplates.map(it => `<option value="${it.id}">${it.name}</option>`).join('');
  // Build list of option rows with a checkbox and a param input. The
  // param input is disabled until its checkbox is ticked. Each row has
  // class gc-opt-checkbox and gc-opt-param for easy selection.
  const optionRowsHtml = optionTemplates.map(opt => {
    return `
      <div class="giftcode-option-row" style="margin-bottom:6px;">
        <label><input type="checkbox" class="gc-opt-checkbox" value="${opt.id}"> ${opt.description}</label>
        <input type="number" class="gc-opt-param" placeholder="param" style="width:60px; margin-left:4px;" disabled>
      </div>
    `;
  }).join('');
  form.innerHTML = `
    <div style="margin-bottom:10px;">
      <label>Giftcode:</label><br>
      <input type="text" id="giftcode-code" style="width:100%;">
    </div>
    <div style="margin-bottom:10px;">
      <label>Số lần sử dụng (countLeft):</label><br>
      <input type="number" id="giftcode-count" style="width:100%;" value="1" min="1">
    </div>
    <div style="margin-bottom:10px;">
      <label>Loại giftcode (type):</label><br>
      <select id="giftcode-type" style="width:100%;">
        <option value="0">0</option>
        <option value="1">1</option>
      </select>
    </div>
    <div style="margin-bottom:10px;">
      <label>Ngày hết hạn (expired):</label><br>
      <input type="date" id="giftcode-expired" style="width:100%;">
    </div>
    <div style="margin-bottom:10px;">
      <label>Chọn item:</label><br>
      <select id="giftcode-item" style="width:100%;">
        ${itemOptionsHtml}
      </select>
    </div>
    <div style="margin-bottom:10px;">
      <label>Số lượng:</label><br>
      <input type="number" id="giftcode-quantity" style="width:100%;" value="1" min="1">
    </div>
    <div style="margin-bottom:10px;">
      <label>Chọn options và nhập param:</label><br>
      <div id="giftcode-options" style="max-height:150px; overflow-y:auto; border:1px solid #ddd; padding:4px;">
        ${optionRowsHtml}
      </div>
    </div>
    <button id="create-giftcode">Tạo Giftcode</button>
    <p id="giftcode-message" style="color:#c00;"></p>
  `;
  container.appendChild(form);
  // Enable or disable param input when option checkbox changes
  form.querySelectorAll('.gc-opt-checkbox').forEach(cb => {
    cb.addEventListener('change', () => {
      const containerDiv = cb.closest('.giftcode-option-row');
      const paramInput = containerDiv.querySelector('.gc-opt-param');
      if (!paramInput) return;
      paramInput.disabled = !cb.checked;
      if (!cb.checked) paramInput.value = '';
    });
  });

  document.getElementById('create-giftcode').addEventListener('click', () => {
    const code = document.getElementById('giftcode-code').value.trim();
    const itemId = parseInt(document.getElementById('giftcode-item').value, 10);
    const quantityVal = parseInt(document.getElementById('giftcode-quantity').value, 10);
    const optsContainer = document.getElementById('giftcode-options');
    const selectedOpts = [];
    optsContainer.querySelectorAll('.gc-opt-checkbox').forEach(cb => {
      if (cb.checked) {
        const id = parseInt(cb.value, 10);
        const row = cb.closest('.giftcode-option-row');
        const paramInput = row.querySelector('.gc-opt-param');
        const param = parseInt(paramInput.value, 10);
        if (!isNaN(id) && !isNaN(param)) {
          selectedOpts.push({ id, param });
        }
      }
    });
    const msgEl = document.getElementById('giftcode-message');
    if (!code || isNaN(itemId)) {
      msgEl.style.color = '#c00';
      msgEl.textContent = 'Vui lòng nhập giftcode và chọn item.';
      return;
    }
    // Collect additional fields
    const countLeft = parseInt(document.getElementById('giftcode-count').value, 10);
    const typeVal = parseInt(document.getElementById('giftcode-type').value, 10);
    const expiredDate = document.getElementById('giftcode-expired').value;
    const payload = {
      code,
      items: [
        {
          temp_id: itemId,
          quantity: isNaN(quantityVal) ? 1 : quantityVal,
          options: selectedOpts
        }
      ],
      countLeft: isNaN(countLeft) ? 1 : countLeft,
      type: isNaN(typeVal) ? 0 : typeVal,
      expired: expiredDate || null
    };
    fetch('/api/giftcodes', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(res => res.json())
      .then(data => {
        if (data.success) {
          msgEl.style.color = '#090';
          msgEl.textContent = 'Tạo giftcode thành công!';
          document.getElementById('giftcode-code').value = '';
          document.getElementById('giftcode-count').value = '1';
          document.getElementById('giftcode-type').value = '0';
          document.getElementById('giftcode-expired').value = '';
          // Reset quantity and item selection
          document.getElementById('giftcode-quantity').value = '1';
          // Reset options
          optsContainer.querySelectorAll('.gc-opt-checkbox').forEach(cb => cb.checked = false);
          optsContainer.querySelectorAll('.gc-opt-param').forEach(inp => { inp.disabled = true; inp.value = ''; });
          // Reload giftcode list to include the new giftcode
          loadGiftcodesList();
        } else {
          msgEl.style.color = '#c00';
          msgEl.textContent = data.error || 'Có lỗi.';
        }
      })
      .catch(err => {
        msgEl.style.color = '#c00';
        msgEl.textContent = 'Có lỗi khi gửi yêu cầu.';
        console.error(err);
      });
  });
}

/**
 * Tải danh sách giftcode từ API và hiển thị trong giftcode list section.
 */
function loadGiftcodesList() {
  fetch('/api/giftcodes')
    .then(res => res.json())
    .then(data => {
      giftcodesData = Array.isArray(data) ? data : [];
      renderGiftcodesList();
    })
    .catch(err => {
      console.error(err);
      const listEl = document.getElementById('giftcode-list');
      if (listEl) listEl.innerHTML = '<p>Lỗi tải giftcode.</p>';
    });
}

/**
 * Hiển thị danh sách giftcode. Mỗi giftcode có thể mở rộng để xem các
 * vật phẩm bên trong bằng cách click.
 */
function renderGiftcodesList() {
  const listEl = document.getElementById('giftcode-list');
  if (!listEl) return;
  listEl.innerHTML = '';
  // Show message when no giftcodes exist
  if (!giftcodesData || giftcodesData.length === 0) {
    listEl.innerHTML = '<p>Chưa có giftcode nào.</p>';
    return;
  }
  // For each giftcode, create a card with a clickable header and a collapsible details section
  giftcodesData.forEach(gfc => {
    const card = document.createElement('div');
    card.className = 'bg-white border border-gray-300 rounded p-4 shadow-sm mb-4';
    // Header shows code and remaining uses.  Clicking toggles the details below.
    const headerEl = document.createElement('div');
    headerEl.className = 'flex justify-between items-center cursor-pointer';
    headerEl.innerHTML = `
      <div class="font-semibold">${gfc.code}</div>
      <div class="text-sm text-gray-600">${gfc.countLeft} lần</div>
    `;
    // Details container, initially hidden
    const detailsEl = document.createElement('div');
    detailsEl.className = 'giftcode-details mt-2 hidden';
    // Build items grid similar to shop items.  Each option description is placed on its own line.
    let itemsGrid;
    if (gfc.items && gfc.items.length > 0) {
      itemsGrid = '<div class="grid grid-cols-2 md:grid-cols-3 gap-2">' + gfc.items.map(it => {
        const optsDesc = (it.options && it.options.length > 0)
          ? it.options.map(o => o.description).join('<br>')
          : '';
        return `
          <div class="flex items-start p-2 border border-gray-200 rounded">
            <img src="${it.icon}" alt="${it.name}" class="w-8 h-8 mr-2">
            <div class="text-sm">
              <div class="font-medium">${it.name}</div>
              <div class="text-gray-600">x${it.quantity}</div>
              ${optsDesc ? `<div class="text-xs text-gray-500">${optsDesc}</div>` : ''}
            </div>
          </div>
        `;
      }).join('') + '</div>';
    } else {
      itemsGrid = '<p class="text-sm text-gray-700">Không có vật phẩm.</p>';
    }
    detailsEl.innerHTML = itemsGrid;
    // Toggle visibility on header click
    headerEl.addEventListener('click', () => {
      detailsEl.classList.toggle('hidden');
    });
    card.appendChild(headerEl);
    card.appendChild(detailsEl);
    listEl.appendChild(card);
  });
}

/**
 * Hiển thị section được chọn và ẩn các section khác. Đồng thời tải dữ liệu
 * cần thiết cho section đó.
 * @param {string} section One of 'shop', 'players', 'giftcode'
 */
function showSection(section) {
  const sections = {
    shop: document.getElementById('shop-section'),
    players: document.getElementById('players-section'),
    giftcode: document.getElementById('giftcode-section')
  };
  for (const key in sections) {
    sections[key].style.display = key === section ? 'block' : 'none';
  }
  // Highlight nav button
  const navs = {
    shop: document.getElementById('nav-shop'),
    players: document.getElementById('nav-players'),
    giftcode: document.getElementById('nav-giftcode')
  };
  for (const key in navs) {
    navs[key].style.background = key === section ? '#555' : '#444';
  }
  // Load data when switching
  if (section === 'players') {
    loadPlayers();
  } else if (section === 'giftcode') {
    // Load both giftcode list and giftcode form when switching to giftcode tab
    loadGiftcodesList();
    loadGiftcodeForm();
  }
}

// Initialize loading after DOM is ready
document.addEventListener('DOMContentLoaded', () => {
  // Bind navigation buttons
  document.getElementById('nav-shop').addEventListener('click', () => showSection('shop'));
  document.getElementById('nav-players').addEventListener('click', () => showSection('players'));
  document.getElementById('nav-giftcode').addEventListener('click', () => showSection('giftcode'));
  // Load initial shop data and show shop section by default
  loadShopsFromApi();
  showSection('shop');
});