import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { useAuth } from "../../features/auth/AuthContext";
import { useCreateReview, useProductReviews } from "../../hooks/useReviews";
import type { RatingValue } from "../../types/engagement";
import { apiErrorMessage } from "../../utils/apiError";

const schema = z.object({
  rating: z.coerce.number().int().min(1).max(5),
  comment: z.string().max(2000).optional(),
});
type FormValues = z.infer<typeof schema>;

export default function ReviewSection({ productId }: { productId: number }) {
  const [page, setPage] = useState(0);
  const auth = useAuth();
  const reviews = useProductReviews(productId, page);
  const create = useCreateReview(productId);
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { rating: 5, comment: "" },
  });

  const submit = form.handleSubmit((values) => {
    create.mutate(
      { rating: values.rating as RatingValue, comment: values.comment },
      { onSuccess: () => form.reset() },
    );
  });

  return (
    <section className="mt-14 border-t pt-10">
      <h2 className="text-2xl font-bold">Customer reviews</h2>
      <p className="mt-1 text-slate-600">
        {reviews.data
          ? `${reviews.data.averageRating.toFixed(1)} / 5 from ${reviews.data.reviewCount} reviews`
          : "Loading rating…"}
      </p>

      {auth.user?.role === "CUSTOMER" && (
        <form className="mt-6 rounded-xl border bg-white p-5" onSubmit={submit}>
          <h3 className="font-bold">Write a review</h3>
          <p className="mt-1 text-sm text-slate-500">
            A delivered purchase is required. The server verifies eligibility and prevents duplicate reviews.
          </p>
          <label className="mt-4 block font-medium" htmlFor="rating">Rating</label>
          <select className="mt-1 rounded border p-2" id="rating" {...form.register("rating")}>
            {[5, 4, 3, 2, 1].map((rating) => <option key={rating} value={rating}>{rating} stars</option>)}
          </select>
          <label className="mt-4 block font-medium" htmlFor="comment">Comment (optional)</label>
          <textarea className="mt-1 w-full rounded border p-2" id="comment" rows={4} {...form.register("comment")} />
          {form.formState.errors.comment && <p role="alert">{form.formState.errors.comment.message}</p>}
          {create.isError && (
            <p className="mt-2 text-red-700" role="alert">{apiErrorMessage(create.error, "Review could not be saved")}</p>
          )}
          <button className="mt-4 rounded bg-brand-700 px-4 py-2 text-white disabled:opacity-50" disabled={create.isPending}>
            {create.isPending ? "Saving…" : "Save review"}
          </button>
        </form>
      )}

      {reviews.isError ? (
        <p className="mt-6 text-red-700" role="alert">{apiErrorMessage(reviews.error, "Could not load reviews")}</p>
      ) : reviews.data?.content.length === 0 ? (
        <p className="mt-6 rounded-xl bg-slate-50 p-6">No reviews yet.</p>
      ) : (
        <div className="mt-6 space-y-4">
          {reviews.data?.content.map((review) => (
            <article className="rounded-xl border bg-white p-5" key={review.id}>
              <div className="flex justify-between"><strong>{review.customerName}</strong><span>{review.rating} / 5</span></div>
              {review.comment && <p className="mt-3 text-slate-700">{review.comment}</p>}
              <time className="mt-3 block text-xs text-slate-500">{new Date(review.createdAt).toLocaleDateString()}</time>
            </article>
          ))}
        </div>
      )}
      {reviews.data && reviews.data.totalPages > 1 && (
        <div className="mt-5 flex gap-3">
          <button disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Previous</button>
          <button disabled={page + 1 >= reviews.data.totalPages} onClick={() => setPage((value) => value + 1)}>Next</button>
        </div>
      )}
    </section>
  );
}
