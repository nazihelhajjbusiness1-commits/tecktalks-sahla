import { useRef, useState, type ChangeEvent } from 'react'
import { Camera, ImagePlus, Trash2 } from 'lucide-react'
import {
  Card,
  CardHeader,
  Button,
  LoadingSpinner,
  EmptyState,
  Alert,
  AuthImage,
  Modal,
} from '@/components/common'
import { useAsync } from '@/hooks/useAsync'
import { deliveryService } from '@/services/deliveryService'
import { ApiError } from '@/services/api'
import { formatDateTime } from '@/utils/format'
import type { DeliveryPhotoResponse } from '@/types'

interface DeliveryPhotosProps {
  deliveryId: number
  /** Whether the current user may add/remove photos (viewing is always allowed). */
  canUpload: boolean
  /** Compact strip (no Card chrome) for embedding, e.g. inside the grade modal. */
  compact?: boolean
}

/**
 * Produce photos for a delivery — inspection evidence that loads inline in the
 * app. Everyone with delivery access sees the thumbnails (click to enlarge);
 * receiving/inspector/manager/admin can also add and remove them.
 */
export function DeliveryPhotos({
  deliveryId,
  canUpload,
  compact = false,
}: DeliveryPhotosProps) {
  const [reloadKey, setReloadKey] = useState(0)
  const { data, loading, error } = useAsync(
    () => deliveryService.listPhotos(deliveryId),
    [deliveryId, reloadKey],
  )
  const photos = data ?? []

  const fileInputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [uploadError, setUploadError] = useState<string | null>(null)
  const [active, setActive] = useState<DeliveryPhotoResponse | null>(null)
  const [deleting, setDeleting] = useState(false)

  const reload = () => setReloadKey((k) => k + 1)

  async function onPick(e: ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0]
    e.target.value = '' // let the same file be re-picked after an error
    if (!file) return
    setUploadError(null)
    setUploading(true)
    try {
      await deliveryService.uploadPhoto(deliveryId, file)
      reload()
    } catch (err) {
      setUploadError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not upload the photo.',
      )
    } finally {
      setUploading(false)
    }
  }

  async function onDelete(photo: DeliveryPhotoResponse) {
    setDeleting(true)
    try {
      await deliveryService.deletePhoto(deliveryId, photo.id)
      setActive(null)
      reload()
    } catch (err) {
      setUploadError(
        err instanceof ApiError || err instanceof Error
          ? err.message
          : 'Could not remove the photo.',
      )
    } finally {
      setDeleting(false)
    }
  }

  const addButton = canUpload ? (
    <>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp,image/gif"
        className="hidden"
        onChange={onPick}
      />
      <Button
        size="sm"
        icon={ImagePlus}
        loading={uploading}
        onClick={() => fileInputRef.current?.click()}
      >
        Add photo
      </Button>
    </>
  ) : null

  const body = (
    <>
      {uploadError && (
        <Alert variant="danger" className="mb-4" onDismiss={() => setUploadError(null)}>
          {uploadError}
        </Alert>
      )}

      {loading ? (
        <div className="flex justify-center py-6">
          <LoadingSpinner label="Loading photos…" />
        </div>
      ) : error ? (
        <Alert variant="danger">{error}</Alert>
      ) : photos.length === 0 ? (
        compact ? (
          <p className="py-2 text-sm text-muted-foreground">No photos attached yet.</p>
        ) : (
          <EmptyState
            icon={Camera}
            title="No photos yet"
            description={
              canUpload
                ? 'Attach a photo of the produce so the inspector can review it.'
                : 'Photos of the produce will appear here once added.'
            }
            action={addButton ?? undefined}
          />
        )
      ) : (
        <ul
          className={
            compact
              ? 'flex gap-2 overflow-x-auto pb-1'
              : 'grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4'
          }
        >
          {photos.map((photo) => (
            <li key={photo.id} className={compact ? 'shrink-0' : undefined}>
              <button
                type="button"
                onClick={() => setActive(photo)}
                className="group block overflow-hidden rounded-md border border-border focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
                title={photo.originalFilename ?? 'Produce photo'}
              >
                <AuthImage
                  loaderKey={photo.id}
                  loader={() => deliveryService.photoObjectUrl(deliveryId, photo.id)}
                  alt={photo.originalFilename ?? `Produce photo ${photo.id}`}
                  className={
                    compact
                      ? 'h-24 w-24 object-cover transition-transform group-hover:scale-105'
                      : 'aspect-square w-full object-cover transition-transform group-hover:scale-105'
                  }
                />
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  )

  const lightbox = active && (
        <Modal
          open
          size="lg"
          onClose={() => (deleting ? undefined : setActive(null))}
          title={active.originalFilename ?? 'Produce photo'}
          description={
            [active.uploadedBy, formatDateTime(active.createdAt)]
              .filter(Boolean)
              .join(' · ') || undefined
          }
          footer={
            <>
              <Button variant="outline" onClick={() => setActive(null)} disabled={deleting}>
                Close
              </Button>
              {canUpload && (
                <Button
                  variant="danger"
                  icon={Trash2}
                  loading={deleting}
                  onClick={() => onDelete(active)}
                >
                  Delete
                </Button>
              )}
            </>
          }
        >
          <AuthImage
            loaderKey={active.id}
            loader={() => deliveryService.photoObjectUrl(deliveryId, active.id)}
            alt={active.originalFilename ?? `Produce photo ${active.id}`}
            className="max-h-[70vh] w-full rounded-md object-contain"
          />
        </Modal>
  )

  if (compact) {
    return (
      <div>
        <div className="mb-2 flex items-center justify-between gap-3">
          <h3 className="text-sm font-medium text-foreground">Produce photos</h3>
          {addButton}
        </div>
        {body}
        {lightbox}
      </div>
    )
  }

  return (
    <Card flush>
      <CardHeader title="Photos" description="Produce photos for inspection" action={addButton ?? undefined} />
      <div className="p-5">{body}</div>
      {lightbox}
    </Card>
  )
}
