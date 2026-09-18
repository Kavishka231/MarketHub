export default function ProductGallery({ imageUrl, name }: { imageUrl: string | null; name: string }) {
  return <div className="aspect-square overflow-hidden rounded-2xl bg-slate-100">{imageUrl ? <img className="h-full w-full object-cover" src={imageUrl} alt={name} /> : <div className="flex h-full items-center justify-center text-lg text-slate-400">Image not available</div>}</div>;
}
