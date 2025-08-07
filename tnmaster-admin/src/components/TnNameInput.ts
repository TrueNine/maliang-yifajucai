export function splitName(name: string) {
  let firstName = ''
  let lastName = ''
  if (!name) {
    return { firstName, lastName }
  }
  name = name.trim()
  if (name.length === 1) {
    firstName = name
  } else if (name.length === 2) {
    firstName = name.slice(0, 1)
    lastName = name.slice(1)
  } else if (name.length === 3) {
    firstName = name.slice(0, 1)
    lastName = name.slice(1, 3)
  } else if (name.length === 4) {
    if (name.slice(2, 3) === ' ') {
      firstName = name.slice(0, 2)
      lastName = name.slice(3)
    } else {
      firstName = name.slice(0, 2)
      lastName = name.slice(2)
    }
  } else {
    const half = Math.floor(name.length / 2)
    firstName = name.slice(0, half)
    lastName = name.slice(half)
  }
  return { firstName, lastName }
}
