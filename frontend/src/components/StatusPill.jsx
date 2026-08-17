const LABELS = {
  AVAILABLE: 'Available',
  IN_USE: 'In Use',
  REPAIR: 'In Repair',
}

// Label text follows the task's own wording (Available/In Use/Repair), not the
// wireframe's "Rented" — see the README's "Wireframe deviations" section.
export function StatusPill({ status }) {
  return (
    <span className={`status-pill status-pill--${status.toLowerCase()}`}>
      {LABELS[status] ?? status}
    </span>
  )
}
