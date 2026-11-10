// 无图商品封面：按分类映射到一块低饱和色块（比灰色“无图”更有存在感）
const TONES = {
  数码: 'digital',
  书籍: 'book',
  生活用品: 'life',
  服饰: 'clothes',
  运动: 'sport'
}

export function coverClass(category) {
  return 'cover-ph--' + (TONES[category] || 'other')
}
