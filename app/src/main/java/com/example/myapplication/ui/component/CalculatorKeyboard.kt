package com.example.myapplication.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 计算器键盘组件
 * 提供数字输入和基本运算功能
 */
@Composable
fun CalculatorKeyboard(
    onNumberClick: (String) -> Unit,
    onOperatorClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    onEqualsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 第一行：7, 8, 9, 删除
            KeyboardRow {
                CalculatorButton("7", { onNumberClick("7") })
                CalculatorButton("8", { onNumberClick("8") })
                CalculatorButton("9", { onNumberClick("9") })
                CalculatorIconButton(
                    icon = Icons.Default.Backspace,
                    onClick = onDeleteClick,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // 第二行：4, 5, 6, +
            KeyboardRow {
                CalculatorButton("4", { onNumberClick("4") })
                CalculatorButton("5", { onNumberClick("5") })
                CalculatorButton("6", { onNumberClick("6") })
                CalculatorButton(
                    text = "+",
                    onClick = { onOperatorClick("+") },
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 第三行：1, 2, 3, -
            KeyboardRow {
                CalculatorButton("1", { onNumberClick("1") })
                CalculatorButton("2", { onNumberClick("2") })
                CalculatorButton("3", { onNumberClick("3") })
                CalculatorButton(
                    text = "-",
                    onClick = { onOperatorClick("-") },
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // 第四行：C, 0, ., 确认
            KeyboardRow {
                CalculatorButton(
                    text = "C",
                    onClick = onClearClick,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
                CalculatorButton("0", { onNumberClick("0") })
                CalculatorButton(".", { onNumberClick(".") })
                CalculatorButton(
                    text = "=",
                    onClick = onEqualsClick,
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * 键盘行容器，确保按钮均匀分布
 */
@Composable
private fun KeyboardRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

/**
 * 计算器按钮组件
 */
@Composable
private fun RowScope.CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .weight(1f)
            .aspectRatio(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 计算器图标按钮组件
 */
@Composable
private fun RowScope.CalculatorIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .weight(1f)
            .aspectRatio(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}
