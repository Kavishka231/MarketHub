package com.markethub.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class AuditMutationAspect {
    private final AuditService audit;

    public AuditMutationAspect(AuditService audit) {
        this.audit = audit;
    }

    @Around("execution(public * com.markethub.admin.AdminUserService.disable(..)) || "
            + "execution(public * com.markethub.admin.AdminUserService.enable(..)) || "
            + "execution(public * com.markethub.vendor.VendorService.approve(..)) || "
            + "execution(public * com.markethub.vendor.VendorService.reject(..)) || "
            + "execution(public * com.markethub.vendor.VendorService.suspend(..)) || "
            + "execution(public * com.markethub.admin.AdminProductService.deactivate(..)) || "
            + "execution(public * com.markethub.product.ProductService.create(..)) || "
            + "execution(public * com.markethub.product.VendorProductManagementService.update(..)) || "
            + "execution(public * com.markethub.product.VendorProductManagementService.archive(..)) || "
            + "execution(public * com.markethub.order.VendorOrderItemStatusService.update(..)) || "
            + "execution(public * com.markethub.order.CheckoutService.checkout(..)) || "
            + "execution(public * com.markethub.order.OrderCancellationService.cancel(..)) || "
            + "execution(public * com.markethub.review.ReviewService.create(..)) || "
            + "execution(public * com.markethub.review.ReviewService.update(..)) || "
            + "execution(public * com.markethub.review.ReviewService.delete(..))")
    public Object auditMutation(ProceedingJoinPoint joinPoint) throws Throwable {
        Descriptor descriptor = Descriptor.from(joinPoint);
        try {
            Object result = joinPoint.proceed();
            audit.record(descriptor.action(), descriptor.resourceType(), resolveResourceId(joinPoint, result), "SUCCESS");
            return result;
        } catch (Throwable throwable) {
            audit.record(descriptor.action(), descriptor.resourceType(), resolveArgumentId(joinPoint), "FAILURE");
            throw throwable;
        }
    }

    private String resolveResourceId(ProceedingJoinPoint joinPoint, Object result) {
        if (result != null) {
            for (String methodName : new String[]{"id", "orderId"}) {
                try {
                    Method method = result.getClass().getMethod(methodName);
                    Object value = method.invoke(result);
                    if (value != null) return value.toString();
                } catch (ReflectiveOperationException ignored) {
                    // Response does not expose this identifier shape.
                }
            }
        }
        return resolveArgumentId(joinPoint);
    }

    private String resolveArgumentId(ProceedingJoinPoint joinPoint) {
        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Long value) return value.toString();
        }
        return null;
    }

    record Descriptor(String action, String resourceType) {
        static Descriptor from(ProceedingJoinPoint point) {
            String key = point.getTarget().getClass().getSimpleName() + "." + point.getSignature().getName();
            return switch (key) {
                case "AdminUserService.disable" -> new Descriptor("ADMIN_USER_DISABLE", "USER");
                case "AdminUserService.enable" -> new Descriptor("ADMIN_USER_ENABLE", "USER");
                case "VendorService.approve" -> new Descriptor("ADMIN_VENDOR_APPROVE", "VENDOR");
                case "VendorService.reject" -> new Descriptor("ADMIN_VENDOR_REJECT", "VENDOR");
                case "VendorService.suspend" -> new Descriptor("ADMIN_VENDOR_SUSPEND", "VENDOR");
                case "AdminProductService.deactivate" -> new Descriptor("ADMIN_PRODUCT_DEACTIVATE", "PRODUCT");
                case "ProductService.create" -> new Descriptor("VENDOR_PRODUCT_CREATE", "PRODUCT");
                case "VendorProductManagementService.update" -> new Descriptor("VENDOR_PRODUCT_UPDATE", "PRODUCT");
                case "VendorProductManagementService.archive" -> new Descriptor("VENDOR_PRODUCT_ARCHIVE", "PRODUCT");
                case "VendorOrderItemStatusService.update" -> new Descriptor("VENDOR_ORDER_STATUS_UPDATE", "ORDER_ITEM");
                case "CheckoutService.checkout" -> new Descriptor("CUSTOMER_CHECKOUT", "ORDER");
                case "OrderCancellationService.cancel" -> new Descriptor("CUSTOMER_ORDER_CANCEL", "ORDER");
                case "ReviewService.create" -> new Descriptor("CUSTOMER_REVIEW_CREATE", "REVIEW");
                case "ReviewService.update" -> new Descriptor("CUSTOMER_REVIEW_UPDATE", "REVIEW");
                case "ReviewService.delete" -> new Descriptor("CUSTOMER_REVIEW_DELETE", "REVIEW");
                default -> throw new IllegalStateException("Unmapped audited mutation: " + key);
            };
        }
    }
}