package nro.models.cpanel;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * {@link FlowLayout} biết <b>xuống dòng</b>.
 *
 * <p>FlowLayout gốc vẫn xếp các nút thành một hàng khi tính kích thước mong muốn, nên panel
 * nằm trong {@code BorderLayout.NORTH} chỉ cao đúng một hàng: nút nào vượt quá bề ngang cửa sổ
 * là <b>bị cắt mất, không bấm được</b>. Đây chính là lỗi "nút bị ẩn" ở tab Boss của cpanel khi
 * số nút nhiều lên.
 *
 * <p>Lớp này tính lại chiều cao theo bề ngang thật của khung chứa, nên panel tự cao thêm và
 * các nút xuống hàng dưới thay vì biến mất.
 *
 * <p>Bản chuẩn quen thuộc của Rob Camick, giữ nguyên cách làm.
 */
class WrapLayout extends FlowLayout {

    WrapLayout() {
        super();
    }

    WrapLayout(int align) {
        super(align);
    }

    WrapLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        Dimension minimum = layoutSize(target, false);
        minimum.width -= (getHgap() + 1);
        return minimum;
    }

    private Dimension layoutSize(Container target, boolean preferred) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE;
            }
            int hgap = getHgap();
            int vgap = getVgap();
            Insets insets = target.getInsets();
            int horizontalInsetsAndGap = insets.left + insets.right + (hgap * 2);
            int maxWidth = targetWidth - horizontalInsetsAndGap;

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0;
            int rowHeight = 0;

            int nmembers = target.getComponentCount();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (!m.isVisible()) {
                    continue;
                }
                Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                if (rowWidth + d.width > maxWidth) {
                    addRow(dim, rowWidth, rowHeight);
                    rowWidth = 0;
                    rowHeight = 0;
                }
                if (rowWidth != 0) {
                    rowWidth += hgap;
                }
                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }
            addRow(dim, rowWidth, rowHeight);

            dim.width += horizontalInsetsAndGap;
            dim.height += insets.top + insets.bottom + vgap * 2;

            // Nằm trong JScrollPane thì trừ bớt một chút cho khỏi hiện thanh cuộn ngang thừa.
            Container scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
            if (scrollPane != null && target.isValid()) {
                dim.width -= (hgap + 1);
            }
            return dim;
        }
    }

    private void addRow(Dimension dim, int rowWidth, int rowHeight) {
        dim.width = Math.max(dim.width, rowWidth);
        if (dim.height > 0) {
            dim.height += getVgap();
        }
        dim.height += rowHeight;
    }
}
