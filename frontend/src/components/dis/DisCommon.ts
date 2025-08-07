export function getColor(num: number) {
  switch (num + 1) {
    case 0:
      return 'gray'
    case 1:
      return 'red'
    case 2:
      return 'orange'
    case 3:
      return 'yellow'
    case 4:
      return 'green'
    case 5:
      return 'cyan'
    case 6:
      return 'blue'
    case 7:
      return 'purple'
    default:
      return 'black'
  }
}
export const disNames = ['不接受残障', '视力', '听力', '言语', '肢体', '智力', '精神', '多重']
